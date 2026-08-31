import { makeStore } from "../configureStore";
import { fetchMyDatasetsRequest } from "../../reducers/dataset/datasetReducer";

describe("Redux Store and Saga Middleware", () => {
  it("should create store successfully with saga middleware", () => {
    const store = makeStore();

    // 1. 초기 상태(initialState) 확인
    const state = store.getState();
    expect(state).toHaveProperty("auth");
    expect(state).toHaveProperty("dataset");

    // 2. 사가 태스크(sagaTask)가 정상적으로 등록되었는지 확인
    expect(store.sagaTask).toBeDefined();

    // 3. 액션 디스패치 테스트 (리듀서가 정상 동작하는지 확인)
    store.dispatch(fetchMyDatasetsRequest());
    const updatedState = store.getState();
    expect(updatedState.dataset.loading).toBe(true);
  });
});
// npm test
