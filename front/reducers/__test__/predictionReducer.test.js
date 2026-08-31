import predictionReducer, {
  fetchPredictionsRequest,
  fetchPredictionsSuccess,
  fetchPredictionsFailure,
  predictRequest,
  predictDone,
  predictFailure,
} from "../prediction/predictionReducer";

const initialState = {
  byColumnId: {}, loadingColumnIds: [], predictingColumnIds: [], error: null,
};

describe("predictionReducer", () => {
  it("초기 상태를 반환한다", () => {
    expect(predictionReducer(undefined, { type: "@@INIT" })).toEqual(initialState);
  });

  it("fetchPredictionsRequest: 해당 columnId를 loadingColumnIds에 추가한다", () => {
    const state = predictionReducer(initialState, fetchPredictionsRequest(10));
    expect(state.loadingColumnIds).toEqual([10]);
  });

  it("fetchPredictionsSuccess: 결과를 byColumnId에 저장하고 loadingColumnIds에서 제거한다", () => {
    const prev = { ...initialState, loadingColumnIds: [10] };
    const predictions = [{ predictionId: 1, domainCode: "PHONE_NO" }];
    const state = predictionReducer(prev, fetchPredictionsSuccess({ columnId: 10, predictions }));
    expect(state.byColumnId[10]).toEqual(predictions);
    expect(state.loadingColumnIds).toEqual([]);
  });

  it("fetchPredictionsFailure: loadingColumnIds에서 제거하고 error를 저장한다", () => {
    const prev = { ...initialState, loadingColumnIds: [10] };
    const state = predictionReducer(prev, fetchPredictionsFailure({ columnId: 10, message: "실패" }));
    expect(state.loadingColumnIds).toEqual([]);
    expect(state.error).toBe("실패");
  });

  it("predictRequest: 해당 columnId를 predictingColumnIds에 추가한다", () => {
    const state = predictionReducer(initialState, predictRequest(10));
    expect(state.predictingColumnIds).toEqual([10]);
  });

  it("predictDone: 해당 columnId만 predictingColumnIds에서 제거한다", () => {
    const prev = { ...initialState, predictingColumnIds: [10, 20] };
    const state = predictionReducer(prev, predictDone(10));
    expect(state.predictingColumnIds).toEqual([20]);
  });

  it("predictFailure: predictingColumnIds에서 제거하고 error를 저장한다", () => {
    const prev = { ...initialState, predictingColumnIds: [10] };
    const state = predictionReducer(
      prev,
      predictFailure({ columnId: 10, message: "AI 판별 요청에 실패했습니다." }),
    );
    expect(state.predictingColumnIds).toEqual([]);
    expect(state.error).toBe("AI 판별 요청에 실패했습니다.");
  });
});
