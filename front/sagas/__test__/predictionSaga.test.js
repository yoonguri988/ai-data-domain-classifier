import { call, put } from "redux-saga/effects";
import api from "../../api/axios";
import { fetchPredictionsSaga, predictSaga } from "../prediction/predictionSaga";
import {
  fetchPredictionsSuccess,
  fetchPredictionsFailure,
  predictDone,
  predictFailure,
} from "../../reducers/prediction/predictionReducer";

describe("predictionSaga", () => {
  describe("fetchPredictionsSaga", () => {
    it("조회 성공 시 fetchPredictionsSuccess를 dispatch한다", () => {
      const columnId = 5;
      const gen = fetchPredictionsSaga({ payload: columnId });
      expect(gen.next().value).toEqual(
        call(api.get, `/api/domain-predictions/columns/${columnId}`),
      );

      const predictions = [{ predictionId: 1, domainCode: "PHONE_NO" }];
      expect(gen.next({ data: predictions }).value).toEqual(
        put(fetchPredictionsSuccess({ columnId, predictions })),
      );
    });

    it("조회 실패 시 fetchPredictionsFailure를 dispatch한다", () => {
      const columnId = 5;
      const gen = fetchPredictionsSaga({ payload: columnId });
      gen.next();
      const error = { response: { data: { error: "판별 결과 조회 실패" } } };
      expect(gen.throw(error).value).toEqual(
        put(
          fetchPredictionsFailure({ columnId, message: "판별 결과 조회 실패" }),
        ),
      );
    });
  });

  describe("predictSaga", () => {
    it("판별 요청 성공 시 결과를 저장하고 마지막에 predictDone을 dispatch한다", () => {
      const columnId = 5;
      const gen = predictSaga({ payload: columnId });
      expect(gen.next().value).toEqual(
        call(api.post, `/api/domain-predictions/columns/${columnId}/predict`),
      );

      const predictions = [{ predictionId: 1, domainCode: "PHONE_NO" }];
      expect(gen.next({ data: predictions }).value).toEqual(
        put(fetchPredictionsSuccess({ columnId, predictions })),
      );
      // try 블록이 끝나도 finally는 항상 실행된다
      expect(gen.next().value).toEqual(put(predictDone(columnId)));
      expect(gen.next().done).toBe(true);
    });

    it("판별 요청이 실패해도 predictFailure 뒤에 predictDone을 반드시 dispatch한다", () => {
      const columnId = 5;
      const gen = predictSaga({ payload: columnId });
      gen.next();

      const error = { response: { data: { error: "AI 서버 오류" } } };
      expect(gen.throw(error).value).toEqual(
        put(predictFailure({ columnId, message: "AI 서버 오류" })),
      );
      // catch 이후에도 finally는 실행된다 - predictDone이 빠지면 버튼 로딩 상태가 영원히 안 풀린다.
      expect(gen.next().value).toEqual(put(predictDone(columnId)));
    });
  });
});
