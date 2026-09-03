import { jwtDecode } from "jwt-decode";
import { call, put, takeLatest } from "redux-saga/effects";
import api from "../../api/axios";
import {
  loginRequest,
  loginSuccess,
  loginFailure,
  loadUserRequest,
  loadUserSuccess,
  loadUserFailure,
  logoutRequest,
  logoutDone,
} from "../../reducers/auth/authReducer";

// jwt-decode v4부터 default export가 없어졌다 - `jwtDecode` named export를 써야 한다(api/axios.js의
// decodeRoles 주석 참고). default로 가져오면 undefined가 호출돼 예외가 나고, 아래 catch에 걸려
// roles가 항상 []로 조용히 빠진다.
const decodeRoles = (accessToken) => {
  try {
    return jwtDecode(accessToken).roles || [];
  } catch (e) {
    return [];
  }
};

export function* loginSaga(action) {
  try {
    // action.payload = { loginId, password } (LoginReq)
    const { data } = yield call(api.post, "/auth/login", action.payload);
    yield put(loginSuccess({
      accessToken: data.accessToken,
      user: { ...data.user, roles: decodeRoles(data.accessToken) },
    }));
  } catch (error) {
    const message = error.response?.data?.error || "로그인에 실패했습니다.";
    yield put(loginFailure(message));
  }
}

// 앱이 처음 뜰 때(브라우저 새로고침 포함) 한 번 조용히 시도하는 세션 복원 - accessToken 은 메모리에만
// 있어서 새로고침하면 사라지지만, HttpOnly refreshToken 쿠키가 아직 유효하면 POST /auth/reissue 가 새
// accessToken 과 사용자 정보를 함께 돌려준다(로그인 응답과 같은 LoginRsp 모양이다). 쿠키가 없거나
// 만료됐으면 그냥 로그아웃 상태로 남는다(에러를 화면에 띄우지 않는다 - 사용자 입장에선 "원래 로그인
// 안 한 상태"와 구분이 안 가야 자연스럽다). silent: true 를 넘겨서 api/axios.js 가 이 시도가 실패해도
// 화면을 강제로 옮기지 않게 한다(실패 처리는 아래 catch가 loadUserFailure로 조용히 담당한다).
export function* loadUserSaga() {
  try {
    const { data } = yield call(api.post, "/auth/reissue", null, { silent: true });
    yield put(loadUserSuccess({
      accessToken: data.accessToken,
      user: { ...data.user, roles: decodeRoles(data.accessToken) },
    }));
  } catch (error) {
    yield put(loadUserFailure());
  }
}

export function* logoutSaga() {
  try {
    yield call(api.post, "/auth/logout");
  } catch (error) {
    // 로그아웃 API 호출 자체가 실패해도(네트워크 오류 등) 클라이언트 상태는 그대로 초기화한다 -
    // 서버 쪽 Refresh Token 은 다음 로그인 때 새로 발급되면서 자연히 대체되므로 치명적이지 않다.
  } finally {
    yield put(logoutDone());

    // 예전에는 로그아웃 버튼(Header.js)의 onClick 에서 dispatch(logoutRequest()) 직후에 바로
    // router.push("/auth/login") 를 호출했다 - logoutRequest 는 비동기 saga(여기)라 이 시점엔 아직
    // state.auth.user 가 로그인 상태 그대로 남아있는데, /auth/login 페이지의
    // useEffect(() => { if (user) router.replace("/"); }, [user]) 가 그 stale user 값을 보고 곧바로
    // "/"로 다시 튕겨내 버렸다(경쟁 상태 - 결과적으로 로그아웃 후에도 /dashboard/member 로 돌아가
    // 있는 것처럼 보였다). logoutDone() 이 dispatch 돼 state.auth 가 완전히 초기화된 뒤인 지금 이
    // 시점에 이동시키면 그 문제가 없다. router.push 대신 하드 리다이렉트(location.href)를 쓰는 이유는
    // 이 saga가 컴포넌트 트리 밖이라 router 인스턴스에 접근할 수 없기도 하고, api/axios.js 의 401/
    // reissue 실패 처리에서도 이미 같은 방식을 쓰고 있어서 로그인 페이지로 보내는 방식을 통일한다.
    if (typeof window !== "undefined") {
      window.location.href = "/auth/login";
    }
  }
}

export default function* authSaga() {
  yield takeLatest(loginRequest.type, loginSaga);
  yield takeLatest(loadUserRequest.type, loadUserSaga);
  yield takeLatest(logoutRequest.type, logoutSaga);
}
