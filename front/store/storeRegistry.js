// store/storeRegistry.js
// redux store 인스턴스를 컴포넌트 트리 밖(api/axios.js 인터셉터 등)에서도
// dispatch 할 수 있도록 등록/조회하는 아주 단순한 레지스트리.
// next-redux-wrapper 는 store 를 컴포넌트 props 로 내려주기 때문에
// axios 인터셉터처럼 React 트리 밖에 있는 모듈에서는 store 를 직접 import 할 수 없다.
// 그래서 makeStore() 에서 생성된(브라우저용) 인스턴스를 여기에 등록해두고 꺼내 쓴다.
// accessToken을 Redux(메모리)에만 두기로 하면서(api/axios.js 참고) 이 레지스트리가 실제로
// 쓰이게 됐다 - 요청 인터셉터가 getStore().getState().auth.accessToken을 읽고, 401 재발급
// 성공 시에는 getStore().dispatch(tokenRefreshed(...))로 새 토큰/사용자 정보를 반영한다.

let storeInstance = null;

export const setStore = (store) => {
  storeInstance = store;
};

export const getStore = () => storeInstance;
