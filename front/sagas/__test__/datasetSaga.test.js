import { call, put } from "redux-saga/effects";
import api from "../../api/axios";
import { fetchMyDatasetsSaga, createDatasetSaga } from "../dataset/datasetSaga";
import {
  fetchMyDatasetsSuccess,
  fetchMyDatasetsFailure,
  createDatasetSuccess,
  createDatasetFailure,
} from "../../reducers/dataset/datasetReducer";

describe("datasetSaga", () => {
  describe("fetchMyDatasetsSaga", () => {
    it("목록 조회 성공 시 fetchMyDatasetsSuccess를 dispatch한다", () => {
      const gen = fetchMyDatasetsSaga();
      expect(gen.next().value).toEqual(call(api.get, "/api/datasets/mine"));

      const list = [{ datasetId: 1, datasetName: "고객 마스터" }];
      expect(gen.next({ data: list }).value).toEqual(put(fetchMyDatasetsSuccess(list)));
      expect(gen.next().done).toBe(true);
    });

    it("목록 조회 실패 시 서버 에러 메시지로 fetchMyDatasetsFailure를 dispatch한다", () => {
      const gen = fetchMyDatasetsSaga();
      gen.next();
      const error = { response: { data: { error: "목록을 불러올 수 없습니다." } } };
      expect(gen.throw(error).value).toEqual(put(fetchMyDatasetsFailure("목록을 불러올 수 없습니다.")));
    });
  });

  describe("createDatasetSaga", () => {
    it("등록 성공 시 createDatasetSuccess를 dispatch한다", () => {
      const payload = { datasetName: "고객 마스터", dbSchemaName: "SALES", tableName: "TB_CUSTOMER" };
      const gen = createDatasetSaga({ payload });
      expect(gen.next().value).toEqual(call(api.post, "/api/datasets", payload));

      const created = { datasetId: 10, ...payload };
      expect(gen.next({ data: created }).value).toEqual(put(createDatasetSuccess(created)));
    });

    it("등록 실패 시 기본 에러 메시지로 createDatasetFailure를 dispatch한다", () => {
      const gen = createDatasetSaga({ payload: {} });
      gen.next();
      expect(gen.throw({}).value).toEqual(put(createDatasetFailure("데이터셋 등록에 실패했습니다.")));
    });
  });
});
