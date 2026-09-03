import { call, put, takeLatest } from "redux-saga/effects";
import api from "../../api/axios";
import {
  fetchMyDatasetsRequest,
  fetchMyDatasetsSuccess,
  fetchMyDatasetsFailure,
  createDatasetRequest,
  createDatasetSuccess,
  createDatasetFailure,
} from "../../reducers/dataset/datasetReducer";

// action.payload = { all: boolean } (선택) - 관리자는 모든 사용자의 데이터셋을 볼 수 있어야 해서,
// pages/datasets/index.js가 로그인한 사용자가 ROLE_ADMIN이면 { all: true }를 실어 보낸다. 인자 없이
// 호출하면(일반 사용자) 예전과 동일하게 본인 데이터셋만(/mine) 조회한다.
export function* fetchMyDatasetsSaga(action) {
  const isAdmin = Boolean(action?.payload?.all);
  try {
    const { data } = yield call(api.get, isAdmin ? "/api/datasets/all" : "/api/datasets/mine");
    yield put(fetchMyDatasetsSuccess(data));
  } catch (error) {
    const message = error.response?.data?.error || "데이터셋 목록을 불러오지 못했습니다.";
    yield put(fetchMyDatasetsFailure(message));
  }
}

export function* createDatasetSaga(action) {
  try {
    const { data } = yield call(api.post, "/api/datasets", action.payload);
    yield put(createDatasetSuccess(data));
  } catch (error) {
    const message = error.response?.data?.error || "데이터셋 등록에 실패했습니다.";
    yield put(createDatasetFailure(message));
  }
}

export default function* datasetSaga() {
  yield takeLatest(fetchMyDatasetsRequest.type, fetchMyDatasetsSaga);
  yield takeLatest(createDatasetRequest.type, createDatasetSaga);
}
