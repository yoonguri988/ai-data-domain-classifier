import authReducer, {
  loginRequest,
  loginSuccess,
  loginFailure,
  loadUserRequest,
  loadUserSuccess,
  loadUserFailure,
  tokenRefreshed,
  logoutRequest,
  logoutDone,
} from "../auth/authReducer";

const initialState = {
  accessToken: null,
  user: null,
  initialized: false,
  loading: false,
  error: null,
};

describe("authReducer", () => {
  it("초기 상태를 반환한다", () => {
    expect(authReducer(undefined, { type: "@@INIT" })).toEqual(initialState);
  });

  it("loginRequest: loading을 true로, error를 초기화한다", () => {
    const prev = { ...initialState, error: "이전 에러" };
    const state = authReducer(prev, loginRequest({ loginId: "cyj0703", password: "Passw0rd!23" }));
    expect(state.loading).toBe(true);
    expect(state.error).toBeNull();
  });

  it("loginSuccess: accessToken/user를 저장하고 initialized를 true로 만든다", () => {
    const payload = { accessToken: "token-123", user: { userId: 1, userName: "최윤정", roles: ["ROLE_USER"] } };
    const state = authReducer({ ...initialState, loading: true }, loginSuccess(payload));
    expect(state).toEqual({
      accessToken: "token-123",
      user: payload.user,
      initialized: true,
      loading: false,
      error: null,
    });
  });

  it("loginFailure: 에러 메시지를 저장하고 loading을 false로 만든다", () => {
    const state = authReducer(
      { ...initialState, loading: true },
      loginFailure("아이디 또는 비밀번호가 올바르지 않습니다."),
    );
    expect(state.loading).toBe(false);
    expect(state.error).toBe("아이디 또는 비밀번호가 올바르지 않습니다.");
  });

  it("loadUserRequest: loading을 true로 만든다", () => {
    const state = authReducer(initialState, loadUserRequest());
    expect(state.loading).toBe(true);
  });

  it("loadUserSuccess: accessToken/user를 저장하고 initialized를 true로 만든다", () => {
    const payload = { accessToken: "token-456", user: { userId: 2, userName: "홍길동", roles: ["ROLE_ADMIN"] } };
    const state = authReducer({ ...initialState, loading: true }, loadUserSuccess(payload));
    expect(state.accessToken).toBe("token-456");
    expect(state.user).toEqual(payload.user);
    expect(state.initialized).toBe(true);
    expect(state.loading).toBe(false);
  });

  it("loadUserFailure: accessToken/user를 비우고 initialized는 true로 만든다(에러로 취급하지 않는다)", () => {
    const prev = {
      accessToken: "stale-token", user: { userId: 1 }, initialized: false, loading: true, error: null,
    };
    const state = authReducer(prev, loadUserFailure());
    expect(state).toEqual({
      accessToken: null,
      user: null,
      initialized: true,
      loading: false,
      error: null,
    });
  });

  it("tokenRefreshed: accessToken/user만 갱신하고 나머지는 건드리지 않는다", () => {
    const prev = {
      accessToken: "old", user: { userId: 1 }, initialized: true, loading: false, error: null,
    };
    const payload = { accessToken: "new-token", user: { userId: 1, roles: ["ROLE_USER"] } };
    const state = authReducer(prev, tokenRefreshed(payload));
    expect(state.accessToken).toBe("new-token");
    expect(state.user).toEqual(payload.user);
    expect(state.initialized).toBe(true); // 그대로 유지
  });

  it("logoutRequest: 상태를 바꾸지 않는다(사가가 API 호출을 담당)", () => {
    const prev = {
      accessToken: "token", user: { userId: 1 }, initialized: true, loading: false, error: null,
    };
    const state = authReducer(prev, logoutRequest());
    expect(state).toEqual(prev);
  });

  it("logoutDone: accessToken/user를 비우고 initialized는 true로 유지한다", () => {
    const prev = {
      accessToken: "token", user: { userId: 1 }, initialized: true, loading: false, error: null,
    };
    const state = authReducer(prev, logoutDone());
    expect(state).toEqual({
      accessToken: null,
      user: null,
      initialized: true,
      loading: false,
      error: null,
    });
  });
});
