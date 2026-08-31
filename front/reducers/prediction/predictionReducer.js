import { createSlice } from "@reduxjs/toolkit";

const initialState = {
  // columnId 별로 Top-N 판별결과(DmnPdtRsp[])를 들고 있는다: { [columnId]: DmnPdtRsp[] }
  byColumnId: {},
  loadingColumnIds: [], // 조회 중인 columnId 목록
  predictingColumnIds: [], // "AI 판별하기" 요청 중인 columnId 목록
  error: null,
};

const predictionSlice = createSlice({
  name: "prediction",
  initialState,
  reducers: {
    // action.payload = columnId
    fetchPredictionsRequest: (state, action) => {
      state.loadingColumnIds.push(action.payload);
    },
    fetchPredictionsSuccess: (state, action) => {
      const { columnId, predictions } = action.payload;
      state.loadingColumnIds = state.loadingColumnIds.filter((id) => id !== columnId);
      state.byColumnId[columnId] = predictions;
    },
    fetchPredictionsFailure: (state, action) => {
      const { columnId, message } = action.payload;
      state.loadingColumnIds = state.loadingColumnIds.filter((id) => id !== columnId);
      state.error = message;
    },
    // action.payload = columnId - POST predict 후 재조회(fetchPredictionsRequest)까지 saga가 이어서 한다.
    predictRequest: (state, action) => {
      state.predictingColumnIds.push(action.payload);
    },
    predictDone: (state, action) => {
      state.predictingColumnIds = state.predictingColumnIds.filter((id) => id !== action.payload);
    },
    predictFailure: (state, action) => {
      const { columnId, message } = action.payload;
      state.predictingColumnIds = state.predictingColumnIds.filter((id) => id !== columnId);
      state.error = message;
    },
  },
});

export const {
  fetchPredictionsRequest,
  fetchPredictionsSuccess,
  fetchPredictionsFailure,
  predictRequest,
  predictDone,
  predictFailure,
} = predictionSlice.actions;

export default predictionSlice.reducer;
