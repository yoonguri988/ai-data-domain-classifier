import { call, put, takeEvery } from "redux-saga/effects";
import api from "../../api/axios";
import {
  fetchPendingRequest,
  fetchPendingSuccess,
  fetchPendingFailure,
  reviewRequest,
  reviewSuccess,
  reviewFailure,
} from "../../reducers/approval/approvalReducer";

function* fetchPendingSaga() {
  try {
    const { data } = yield call(api.get, "/api/std-domain-requests/pending");
    yield put(fetchPendingSuccess(data));
  } catch (error) {
    const message = error.response?.data?.error || "승인 대기 목록을 불러오지 못했습니다.";
    yield put(fetchPendingFailure(message));
  }
}

function* reviewSaga(action) {
  const { requestId, approve, rejectReason } = action.payload;
  try {
    yield call(api.patch, `/api/std-domain-requests/${requestId}/review`, { approve, rejectReason });
    yield put(reviewSuccess({ requestId }));
  } catch (error) {
    const message = error.response?.data?.error || "승인/반려 처리에 실패했습니다.";
    yield put(reviewFailure({ requestId, message }));
  }
}

export default function* approvalSaga() {
  yield takeEvery(fetchPendingRequest.type, fetchPendingSaga);
  // 여러 건을 연달아 눌러도(예: 일괄 승인) 각각 독립적으로 처리되도록 takeLatest 대신 takeEvery 를 쓴다.
  yield takeEvery(reviewRequest.type, reviewSaga);
}
