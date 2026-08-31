import { call, put } from "redux-saga/effects";
import api from "../../api/axios";
import { fetchPendingSaga, reviewSaga } from "./approvalSaga";
import {
  fetchPendingSuccess,
  fetchPendingFailure,
  reviewSuccess,
  reviewFailure,
} from "../../reducers/approval/approvalReducer";

describe("approvalSaga", () => {
  describe("fetchPendingSaga", () => {
    it("대기 목록 조회 성공 시 fetchPendingSuccess를 dispatch한다", () => {
      const gen = fetchPendingSaga();
      expect(gen.next().value).toEqual(call(api.get, "/api/std-domain-requests/pending"));

      const list = [{ requestId: 1, columnName: "CUST_PHONE_NO" }];
      expect(gen.next({ data: list }).value).toEqual(put(fetchPendingSuccess(list)));
    });

    it("대기 목록 조회 실패 시 fetchPendingFailure를 dispatch한다", () => {
      const gen = fetchPendingSaga();
      gen.next();
      const error = { response: { data: { error: "목록 조회 실패" } } };
      expect(gen.throw(error).value).toEqual(put(fetchPendingFailure("목록 조회 실패")));
    });
  });

  describe("reviewSaga", () => {
    it("승인 처리 성공 시 reviewSuccess를 dispatch한다", () => {
      const payload = { requestId: 1, approve: true, rejectReason: undefined };
      const gen = reviewSaga({ payload });
      expect(gen.next().value).toEqual(call(
        api.patch,
        "/api/std-domain-requests/1/review",
        { approve: true, rejectReason: undefined },
      ));

      expect(gen.next().value).toEqual(put(reviewSuccess({ requestId: 1 })));
    });

    it("반려 처리 실패 시 서버 에러 메시지로 reviewFailure를 dispatch한다", () => {
      const payload = { requestId: 2, approve: false, rejectReason: "형식이 다릅니다." };
      const gen = reviewSaga({ payload });
      gen.next();

      const error = { response: { data: { error: "이미 처리된 요청입니다." } } };
      expect(gen.throw(error).value)
        .toEqual(put(reviewFailure({ requestId: 2, message: "이미 처리된 요청입니다." })));
    });
  });
});
