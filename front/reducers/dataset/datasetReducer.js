import { createSlice } from "@reduxjs/toolkit";

const initialState = {
  list: [], // AnlDsetRsp[]
  loading: false,
  error: null,
  creating: false,
  createError: null,
};

const datasetSlice = createSlice({
  name: "dataset",
  initialState,
  reducers: {
    fetchMyDatasetsRequest: (state) => {
      state.loading = true;
      state.error = null;
    },
    fetchMyDatasetsSuccess: (state, action) => {
      state.loading = false;
      state.list = action.payload;
    },
    fetchMyDatasetsFailure: (state, action) => {
      state.loading = false;
      state.error = action.payload;
    },
    // action.payload = AnlDsetCreateReq
    createDatasetRequest: (state) => {
      state.creating = true;
      state.createError = null;
    },
    createDatasetSuccess: (state, action) => {
      state.creating = false;
      state.list = [action.payload, ...state.list];
    },
    createDatasetFailure: (state, action) => {
      state.creating = false;
      state.createError = action.payload;
    },
  },
});

export const {
  fetchMyDatasetsRequest,
  fetchMyDatasetsSuccess,
  fetchMyDatasetsFailure,
  createDatasetRequest,
  createDatasetSuccess,
  createDatasetFailure,
} = datasetSlice.actions;

export default datasetSlice.reducer;
