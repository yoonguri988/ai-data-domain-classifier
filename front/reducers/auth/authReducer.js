import { createSlice } from "@reduxjs/toolkit";

// accessToken은 Redux(메모리)에만 둔다 - 쿠키(document.cookie)나 localStorage로 노출해두면 XSS로 스크립트가
// 실행됐을 때 그대로 읽어갈 수 있어서다(메모리는 그럴 수 없고, 새로고침하면 사라져 탈취 유효기간도 짧다).
// 대신 새로고침하면 이 메모리도 같이 사라지는데, Refresh Token 은 HttpOnly 쿠키로만 오가서(JS로 절대 못
// 읽는다 - AuthAcntController 참고) 앱이 뜰 때마다 POST /auth/reissue 를 한 번 조용히 호출해서
// (loadUserRequest, sagas/auth/authSaga.js 참고) 세션을 이어간다.
//
// state.auth = { accessToken, user, initialized, loading, error }
// - user: UsrRsp + roles(Access Token 클레임에서 디코딩해 합친 값) - null이면 비로그인
// - initialized: 세션 복원(loadUserRequest) 시도가 끝났는지 여부. 이게 true가 되기 전에는
//   아직 로그인 여부를 "모르는" 상태이므로 섣불리 /auth/login으로 튕겨보내면 안 된다
//   (pages/index.js, components/AppLayout.js 참고).
const initialState = {
  accessToken: null,
  user: null,
  initialized: false,
  loading: false,
  error: null,
};

const authSlice = createSlice({
  name: "auth",
  initialState,
  reducers: {
    // action.payload = LoginReq { loginId, password }
    loginRequest: (state) => {
      state.loading = true;
      state.error = null;
    },
    // action.payload = { accessToken, user } - user는 이미 roles가 합쳐진 값(saga에서 디코딩)
    loginSuccess: (state, action) => {
      state.loading = false;
      state.accessToken = action.payload.accessToken;
      state.user = action.payload.user;
      state.initialized = true;
    },
    loginFailure: (state, action) => {
      state.loading = false;
      state.error = action.payload;
    },
    // 새로고침 등으로 앱이 처음 뜰 때 한 번 조용히 시도하는 세션 복원(POST /auth/reissue).
    loadUserRequest: (state) => {
      state.loading = true;
    },
    loadUserSuccess: (state, action) => {
      state.loading = false;
      state.accessToken = action.payload.accessToken;
      state.user = action.payload.user;
      state.initialized = true;
    },
    // 실패해도 에러로 취급하지 않는다 - "원래 로그인 안 한 상태"와 구분이 안 가야 자연스럽다.
    loadUserFailure: (state) => {
      state.loading = false;
      state.accessToken = null;
      state.user = null;
      state.initialized = true;
    },
    // api/axios.js가 401 → POST /auth/reissue로 조용히 새 토큰을 받아왔을 때 쓴다
    // (로그인/세션복원과 응답 모양이 같은 LoginRsp라 payload 구조도 동일하다).
    tokenRefreshed: (state, action) => {
      state.accessToken = action.payload.accessToken;
      state.user = action.payload.user;
    },
    logoutRequest: () => {},
    logoutDone: (state) => {
      state.accessToken = null;
      state.user = null;
      state.initialized = true;
    },
  },
});

export const {
  loginRequest,
  loginSuccess,
  loginFailure,
  loadUserRequest,
  loadUserSuccess,
  loadUserFailure,
  tokenRefreshed,
  logoutRequest,
  logoutDone,
} = authSlice.actions;

export default authSlice.reducer;
