import datasetReducer, {
  fetchMyDatasetsRequest,
  fetchMyDatasetsSuccess,
  fetchMyDatasetsFailure,
  createDatasetRequest,
  createDatasetSuccess,
  createDatasetFailure,
} from "./datasetReducer";

const initialState = {
  list: [], loading: false, error: null, creating: false, createError: null,
};

describe("datasetReducer", () => {
  it("초기 상태를 반환한다", () => {
    expect(datasetReducer(undefined, { type: "@@INIT" })).toEqual(initialState);
  });

  it("fetchMyDatasetsRequest: loading을 true로, error를 초기화한다", () => {
    const state = datasetReducer({ ...initialState, error: "이전 에러" }, fetchMyDatasetsRequest());
    expect(state.loading).toBe(true);
    expect(state.error).toBeNull();
  });

  it("fetchMyDatasetsSuccess: 받아온 목록을 그대로 저장한다", () => {
    const list = [{ datasetId: 1, datasetName: "고객 마스터" }];
    const state = datasetReducer({ ...initialState, loading: true }, fetchMyDatasetsSuccess(list));
    expect(state.loading).toBe(false);
    expect(state.list).toEqual(list);
  });

  it("fetchMyDatasetsFailure: 에러 메시지를 저장한다", () => {
    const state = datasetReducer(
      { ...initialState, loading: true },
      fetchMyDatasetsFailure("목록을 불러오지 못했습니다."),
    );
    expect(state.loading).toBe(false);
    expect(state.error).toBe("목록을 불러오지 못했습니다.");
  });

  it("createDatasetRequest: creating을 true로, createError를 초기화한다", () => {
    const state = datasetReducer({ ...initialState, createError: "이전 에러" }, createDatasetRequest());
    expect(state.creating).toBe(true);
    expect(state.createError).toBeNull();
  });

  it("createDatasetSuccess: 새 데이터셋을 목록 맨 앞에 추가한다", () => {
    const existing = [{ datasetId: 1, datasetName: "기존 데이터셋" }];
    const created = { datasetId: 2, datasetName: "새 데이터셋" };
    const state = datasetReducer(
      { ...initialState, list: existing, creating: true },
      createDatasetSuccess(created),
    );
    expect(state.creating).toBe(false);
    expect(state.list).toEqual([created, ...existing]);
  });

  it("createDatasetFailure: createError를 저장한다", () => {
    const state = datasetReducer({ ...initialState, creating: true }, createDatasetFailure("등록에 실패했습니다."));
    expect(state.creating).toBe(false);
    expect(state.createError).toBe("등록에 실패했습니다.");
  });
});
