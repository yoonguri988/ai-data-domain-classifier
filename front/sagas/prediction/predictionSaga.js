import { call, put, takeEvery } from "redux-saga/effects";
import api from "../../api/axios";
import {
  fetchPredictionsRequest,
  fetchPredictionsSuccess,
  fetchPredictionsFailure,
  predictRequest,
  predictDone,
  predictFailure,
} from "../../reducers/prediction/predictionReducer";

// POST /api/domain-predictions/columns/{columnId}/predict 는 동기 호출로 Top-N 결과를 바로 돌려준다
// (DmnPdtController 참고 - 외부 AI API 응답을 기다렸다가 그 자리에서 저장까지 마치고 반환한다).
// 그래서 폴링 없이 한 번의 요청으로 끝난다.

function* fetchPredictionsSaga(action) {
  const columnId = action.payload;
  try {
    const { data } = yield call(api.get, `/api/domain-predictions/columns/${columnId}`);
    yield put(fetchPredictionsSuccess({ columnId, predictions: data }));
  } catch (error) {
    const message = error.response?.data?.error || "판별 결과를 불러오지 못했습니다.";
    yield put(fetchPredictionsFailure({ columnId, message }));
  }
}

function* predictSaga(action) {
  const columnId = action.payload;
  try {
    const { data } = yield call(api.post, `/api/domain-predictions/columns/${columnId}/predict`);
    yield put(fetchPredictionsSuccess({ columnId, predictions: data }));
  } catch (error) {
    const message = error.response?.data?.error || "AI 판별 요청에 실패했습니다.";
    yield put(predictFailure({ columnId, message }));
  } finally {
    yield put(predictDone(columnId));
  }
}

export default function* predictionSaga() {
  yield takeEvery(fetchPredictionsRequest.type, fetchPredictionsSaga);
  yield takeEvery(predictRequest.type, predictSaga);
}
