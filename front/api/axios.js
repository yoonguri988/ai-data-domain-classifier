// api/axios.js
import axios from "axios";
import jwtDecode from "jwt-decode";
import { message } from "antd";
import { getStore } from "../store/storeRegistry";
import { tokenRefreshed } from "../reducers/auth/authReducer";

const api = axios.create({
  // 기본 api 서버 주소, 환경변수 없으면 로컬 서버 사용
  baseURL: process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080",
  // refreshToken 이 HttpOnly 쿠키로만 오가므로 항상 필요
  withCredentials: true,
  headers: {
    "Content-Type": "application/json",
    Accept: "application/json",
  },
});

// LoginRsp.accessToken 안에는 loginId/roles 클레임이 들어 있다(JwtProvider.createAccessToken 참고).
// UsrRsp(로그인/재발급 응답의 user 필드)에는 roles 가 없어서, 화면에서 권한별로 메뉴/버튼을 가리려면
// accessToken 을 직접 디코딩해서 roles 를 꺼내야 한다.
const decodeRoles = (accessToken) => {
  try {
    return jwtDecode(accessToken).roles || [];
  } catch (e) {
    return [];
  }
};

// accessToken은 쿠키나 localStorage가 아니라 Redux(메모리)에만 둔다 - XSS로 스크립트가 실행돼도
// document.cookie/localStorage처럼 그냥 읽어갈 수 없고, 새로고침하면 사라져 탈취돼도 유효기간이 짧다.
// 여기(axios 인터셉터)는 컴포넌트 트리 밖이라 store를 바로 import할 수 없어서, storeRegistry를 통해
// 등록된 store 인스턴스를 꺼내 쓴다(store/storeRegistry.js가 원래 이 용도로 만들어둔 레지스트리다).
api.interceptors.request.use(
  (config) => {
    const store = typeof window !== "undefined" ? getStore() : null;
    const accessToken = store?.getState()?.auth?.accessToken;
    if (accessToken) {
      config.headers.Authorization = `Bearer ${accessToken}`;
    }
    return config;
  },
  (error) => Promise.reject(error),
);

// 동시 요청용 refreshToken 상태 관리
let isRefreshing = false;
let refreshQueue = [];

const processQueue = (error, token = null) => {
  refreshQueue.forEach((p) => {
    if (error) p.reject(error);
    else p.resolve(token);
  });
  refreshQueue = [];
};

api.interceptors.response.use(
  (res) => res,
  async (error) => {
    const original = error.config;
    const status = error.response?.status;

    // /auth/reissue 요청 자체가 실패한 경우 → 재귀 방지, 즉시 종료.
    // config에 silent: true가 있으면(sagas/auth/authSaga.js의 세션 복원처럼 우리가 의도적으로 부른
    // 호출) 방문자가 원래 비로그인 상태일 수 있으니 화면을 강제 이동시키지 않고 saga의
    // catch(loadUserFailure)에 조용히 맡긴다. silent가 아니면(아래 401 재발급 로직이 자동으로 부른
    // 경우) 진짜 세션 만료이므로 로그인 페이지로 보낸다.
    if (original?.url?.includes("/auth/reissue")) {
      if (typeof window !== "undefined" && !original.silent) {
        window.location.href = "/auth/login";
      }
      return Promise.reject(error);
    }

    // 로그인 자체의 401(아이디/비밀번호 오류)은 "세션 만료"가 아니다 - refresh를 시도하면 안 되고
    // saga의 catch(loginFailure)로 그대로 흘려보낸다.
    if (original?.url?.includes("/auth/login")) {
      return Promise.reject(error);
    }

    // 401 발생 시 Refresh Token으로 재발급
    if (status === 401 && !original._retry) {
      // 이미 재발급 진행 중이면 큐에 대기했다가 새 토큰으로 재시도
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          refreshQueue.push({ resolve, reject });
        }).then((newToken) => {
          original.headers.Authorization = `Bearer ${newToken}`;
          return api(original);
        });
      }

      original._retry = true; // 무한 루프 방지 플래그
      isRefreshing = true;

      try {
        const { data } = await api.post("/auth/reissue");
        const newAccessToken = data?.accessToken;

        // 새 토큰뿐 아니라 응답에 같이 실려오는 user(UsrRsp)까지 Redux에 반영해야, 이 시점에 화면을
        // 그리고 있는 다른 컴포넌트들도 최신 사용자 정보를 계속 보게 된다.
        getStore()?.dispatch(
          tokenRefreshed({
            accessToken: newAccessToken,
            user: { ...data.user, roles: decodeRoles(newAccessToken) },
          }),
        );

        processQueue(null, newAccessToken); // 대기 중인 요청 재시도
        original.headers.Authorization = `Bearer ${newAccessToken}`; // 원 요청 헤더 갱신
        return api(original);
      } catch (refreshErr) {
        processQueue(refreshErr, null);
        if (typeof window !== "undefined") {
          window.location.href = "/auth/login"; // 로그인 페이지로 이동
        }
        return Promise.reject(refreshErr);
      } finally {
        isRefreshing = false;
      }
    }

    // 400 안전망: 이 프로젝트는 saga(try/catch) → xxxFailure 액션 → 페이지 컴포넌트의
    // message.error(error) 패턴으로 이미 대부분의 API 에러를 처리하고 있다.
    // 여기서 무조건 토스트를 띄우면 그 위에 또 message.error가 겹쳐서 이중 토스트가 된다.
    // 그래서 기본은 조용히 reject만 하고, saga 없이 컴포넌트에서 바로 api.xxx(...)를
    // 호출하는 등 별도 에러 처리가 없는 호출부에 한해서만 요청 시
    // `notifyOnError: true`를 명시적으로 넘기면 여기서 토스트를 대신 띄워준다.
    //   예) api.post("/api/foo", data, { notifyOnError: true })
    if (status === 400 && original?.notifyOnError) {
      const serverMessage =
        error.response?.data?.error || error.response?.data?.message;
      message.error(
        serverMessage || "요청을 처리할 수 없습니다. 입력값을 확인해주세요.",
      );
    }

    return Promise.reject(error);
  },
);

export default api;
