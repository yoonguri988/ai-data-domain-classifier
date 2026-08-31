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

function* fetchMyDatasetsSaga() {
  try {
    const { data } = yield call(api.get, "/api/datasets/mine");
    yield put(fetchMyDatasetsSuccess(data));
  } catch (error) {
    const message = error.response?.data?.error || "데이터셋 목록을 불러오지 못했습니다.";
    yield put(fetchMyDatasetsFailure(message));
  }
}

function* createDatasetSaga(action) {
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
