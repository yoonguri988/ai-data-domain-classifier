import approvalReducer, {
  fetchPendingRequest,
  fetchPendingSuccess,
  fetchPendingFailure,
  reviewRequest,
  reviewSuccess,
  reviewFailure,
} from "./approvalReducer";

const initialState = {
  pendingList: [], loading: false, error: null, reviewingIds: [],
};

describe("approvalReducer", () => {
  it("초기 상태를 반환한다", () => {
    expect(approvalReducer(undefined, { type: "@@INIT" })).toEqual(initialState);
  });

  it("fetchPendingRequest: loading을 true로, error를 초기화한다", () => {
    const state = approvalReducer({ ...initialState, error: "이전 에러" }, fetchPendingRequest());
    expect(state.loading).toBe(true);
    expect(state.error).toBeNull();
  });

  it("fetchPendingSuccess: 대기 목록을 저장한다", () => {
    const list = [{ requestId: 1, columnName: "CUST_PHONE_NO" }];
    const state = approvalReducer({ ...initialState, loading: true }, fetchPendingSuccess(list));
    expect(state.loading).toBe(false);
    expect(state.pendingList).toEqual(list);
  });

  it("fetchPendingFailure: 에러 메시지를 저장한다", () => {
    const state = approvalReducer(
      { ...initialState, loading: true },
      fetchPendingFailure("목록을 불러오지 못했습니다."),
    );
    expect(state.error).toBe("목록을 불러오지 못했습니다.");
  });

  it("reviewRequest: 해당 requestId를 reviewingIds에 추가한다", () => {
    const state = approvalReducer(initialState, reviewRequest({ requestId: 1, approve: true }));
    expect(state.reviewingIds).toEqual([1]);
  });

  it("reviewSuccess: reviewingIds와 pendingList에서 해당 건을 제거한다", () => {
    const prev = {
      ...initialState,
      reviewingIds: [1],
      pendingList: [{ requestId: 1 }, { requestId: 2 }],
    };
    const state = approvalReducer(prev, reviewSuccess({ requestId: 1 }));
    expect(state.reviewingIds).toEqual([]);
    expect(state.pendingList).toEqual([{ requestId: 2 }]);
  });

  it("reviewFailure: reviewingIds에서 제거하고 error를 저장한다(목록은 그대로 둔다)", () => {
    const prev = {
      ...initialState,
      reviewingIds: [1],
      pendingList: [{ requestId: 1 }],
    };
    const state = approvalReducer(prev, reviewFailure({ requestId: 1, message: "처리에 실패했습니다." }));
    expect(state.reviewingIds).toEqual([]);
    expect(state.pendingList).toEqual([{ requestId: 1 }]);
    expect(state.error).toBe("처리에 실패했습니다.");
  });
});
