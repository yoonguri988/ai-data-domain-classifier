import { combineReducers } from "@reduxjs/toolkit";
import auth from "./auth/authReducer";
import dataset from "./dataset/datasetReducer";
import prediction from "./prediction/predictionReducer";
import approval from "./approval/approvalReducer";

const rootReducer = combineReducers({
  auth,
  dataset,
  prediction,
  approval,
});

export default rootReducer;
