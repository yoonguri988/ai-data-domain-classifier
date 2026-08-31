import { Buffer } from "buffer";
import { call, put } from "redux-saga/effects";
import api from "../../api/axios";
import { loginSaga, loadUserSaga, logoutSaga } from "../auth/authSaga";
import {
  loginSuccess, loginFailure, loadUserSuccess, loadUserFailure, logoutDone,
} from "../../reducers/auth/authReducer";

// jwt-decode는 실제 JWT 형식(header.payload.signature, 각 세그먼트가 base64url)만 디코딩할 수 있어서,
// roles 클레임이 든 것처럼 보이는 가짜 토큰을 만들어 테스트한다(서명 검증은 안 하므로 서명 세그먼트는
// 비워둬도 된다). Buffer는 jest-environment-jsdom(v27+)이 전역으로 안 심어주므로 "buffer" 모듈에서
// 직접 가져온다.
const fakeAccessToken = (payload) => {
  const base64url = (obj) => Buffer.from(JSON.stringify(obj))
    .toString("base64")
    .replace(/\+/g, "-")
    .replace(/\//g, "_")
    .replace(/=+$/, "");
  return `${base64url({ alg: "none", typ: "JWT" })}.${base64url(payload)}.`;
};

describe("authSaga", () => {
  describe("loginSaga", () => {
    it("로그인 성공 시 accessToken/roles가 합쳐진 user로 loginSuccess를 dispatch한다", () => {
      const payload = { loginId: "cyj0703", password: "Passw0rd!23" };
      const accessToken = fakeAccessToken({ loginId: "cyj0703", roles: ["ROLE_USER"] });
      const gen = loginSaga({ payload });

      expect(gen.next().value).toEqual(call(api.post, "/auth/login", payload));

      const response = { data: { accessToken, user: { userId: 1, userName: "최윤정" } } };
      const result = gen.next(response).value;

      expect(result).toEqual(put(loginSuccess({
        accessToken,
        user: { userId: 1, userName: "최윤정", roles: ["ROLE_USER"] },
      })));
      expect(gen.next().done).toBe(true);
    });

    it("로그인 실패 시 서버 에러 메시지로 loginFailure를 dispatch한다", () => {
      const gen = loginSaga({ payload: { loginId: "wrong", password: "wrong" } });
      gen.next(); // call(api.post, "/auth/login", ...)

      const error = { response: { data: { error: "INVALID_CREDENTIALS" } } };
      expect(gen.throw(error).value).toEqual(put(loginFailure("INVALID_CREDENTIALS")));
    });

    it("서버 에러 메시지가 없으면 기본 문구로 loginFailure를 dispatch한다", () => {
      const gen = loginSaga({ payload: { loginId: "x", password: "y" } });
      gen.next();
      expect(gen.throw({}).value).toEqual(put(loginFailure("로그인에 실패했습니다.")));
    });
  });

  describe("loadUserSaga", () => {
    it("세션 복원 성공 시 accessToken/roles가 합쳐진 user로 loadUserSuccess를 dispatch한다", () => {
      const accessToken = fakeAccessToken({ loginId: "cyj0703", roles: ["ROLE_ADMIN"] });
      const gen = loadUserSaga();

      expect(gen.next().value).toEqual(call(api.post, "/auth/reissue", null, { silent: true }));

      const response = { data: { accessToken, user: { userId: 1, userName: "최윤정" } } };
      const result = gen.next(response).value;

      expect(result).toEqual(put(loadUserSuccess({
        accessToken,
        user: { userId: 1, userName: "최윤정", roles: ["ROLE_ADMIN"] },
      })));
    });

    it("refreshToken 쿠키가 없거나 만료됐으면 에러 없이 loadUserFailure만 dispatch한다", () => {
      const gen = loadUserSaga();
      gen.next();
      const result = gen.throw({ response: { status: 401 } }).value;
      expect(result).toEqual(put(loadUserFailure()));
    });
  });

  describe("logoutSaga", () => {
    it("로그아웃 API 호출 후 logoutDone을 dispatch한다", () => {
      const gen = logoutSaga();
      expect(gen.next().value).toEqual(call(api.post, "/auth/logout"));
      expect(gen.next().value).toEqual(put(logoutDone()));
      expect(gen.next().done).toBe(true);
    });

    it("로그아웃 API 호출이 실패해도(네트워크 오류 등) logoutDone은 그대로 dispatch한다", () => {
      const gen = logoutSaga();
      gen.next();
      const result = gen.throw(new Error("network error")).value;
      expect(result).toEqual(put(logoutDone()));
    });
  });
});
