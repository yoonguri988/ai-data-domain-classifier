import { createSlice } from "@reduxjs/toolkit";

const initialState = {
  pendingList: [], // StdDmnReqRsp[] - PENDING 상태만
  loading: false,
  error: null,
  reviewingIds: [], // 승인/반려 처리 중인 requestId 목록 (버튼 로딩 표시용)
};

const approvalSlice = createSlice({
  name: "approval",
  initialState,
  reducers: {
    fetchPendingRequest: (state) => {
      state.loading = true;
      state.error = null;
    },
    fetchPendingSuccess: (state, action) => {
      state.loading = false;
      state.pendingList = action.payload;
    },
    fetchPendingFailure: (state, action) => {
      state.loading = false;
      state.error = action.payload;
    },
    // action.payload = { requestId, approve, rejectReason }
    reviewRequest: (state, action) => {
      state.reviewingIds.push(action.payload.requestId);
    },
    // PENDING 목록이니 승인/반려가 끝난 건은 화면에서 바로 빼준다.
    reviewSuccess: (state, action) => {
      state.reviewingIds = state.reviewingIds.filter((id) => id !== action.payload.requestId);
      state.pendingList = state.pendingList.filter((r) => r.requestId !== action.payload.requestId);
    },
    reviewFailure: (state, action) => {
      state.reviewingIds = state.reviewingIds.filter((id) => id !== action.payload.requestId);
      state.error = action.payload.message;
    },
  },
});

export const {
  fetchPendingRequest,
  fetchPendingSuccess,
  fetchPendingFailure,
  reviewRequest,
  reviewSuccess,
  reviewFailure,
} = approvalSlice.actions;

export default approvalSlice.reducer;
