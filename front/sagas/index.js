import { all } from "redux-saga/effects";
import authSaga from "./auth/authSaga";
import datasetSaga from "./dataset/datasetSaga";
import predictionSaga from "./prediction/predictionSaga";
import approvalSaga from "./approval/approvalSaga";

export default function* rootSaga() {
  yield all([authSaga(), datasetSaga(), predictionSaga(), approvalSaga()]);
}
