// pages/_app.js
import React, { useEffect } from "react";
import { useRouter } from "next/router";
import { useDispatch } from "react-redux";
import { ConfigProvider } from "antd";
import koKR from "antd/lib/locale/ko_KR";
import { wrapper } from "../store/configureStore";
import AppLayout from "../components/AppLayout";
import { loadUserRequest } from "../reducers/auth/authReducer";

// 스타일 (순서 중요: antd → bootstrap(CSS만, 유틸리티 클래스용) → 프로젝트 커스텀 CSS)
// 드롭다운/모달 등 JS로 동작하는 컴포넌트는 전부 antd를 쓰므로 bootstrap.bundle.min.js(JS)는 로드하지 않는다.
// (원본 스캐폴딩에 있던 auth/company/appr/dashboard*/dept/emp/my/notice/perm/project/resv/sal-ai-chat/
//  frappe-gantt/careers 등의 CSS는 이 프로젝트(AI 표준 도메인 추천)와 무관한 다른 참고 프로젝트의
//  화면들이라 가져오지 않았다 - 없는 파일을 import하면 빌드 자체가 깨진다.)
import "antd/dist/antd.css";
import "bootstrap/dist/css/bootstrap.min.css";
import "../styles/global.css";

// 이 프로젝트는 한국어 단일 서비스라 다국어(i18n)를 지원하지 않는다 - 원본 기본 구성에 있던
// react-i18next/I18nextProvider/getStoredLanguage 는 다른 참고 프로젝트의 흔적이라 가져오지 않았다.
// antd의 ConfigProvider locale은 DatePicker/Table 등 antd 컴포넌트 자체의 내장 문구를 한국어로
// 바꾸는 antd 자체 설정이라 i18n 프레임워크와는 무관하다.

// "/auth/**"(로그인/회원가입)와 "/oauth2/**"(소셜 로그인 콜백)는 AppLayout(사이드바 + 로그인 가드)을 타면 안 된다.
const NO_LAYOUT_PREFIXES = ["/auth", "/oauth2"];
// 404/500/_error는 로그인 여부와 무관하게 떠야 하므로 AppLayout(사이드바/헤더) 밖에서 렌더링한다.
const NO_LAYOUT_EXACT = ["/404", "/500", "/_error"];

function MyApp({ Component, pageProps }) {
  const router = useRouter();
  const dispatch = useDispatch();

  const isBareLayout =
    NO_LAYOUT_PREFIXES.some((p) => router.pathname.startsWith(p)) ||
    NO_LAYOUT_EXACT.includes(router.pathname);

  // 세션 복원 - 로그인/콜백 화면에서는 필요 없으므로 생략
  useEffect(() => {
    if (isBareLayout) return;
    dispatch(loadUserRequest());
  }, [dispatch, isBareLayout]);

  if (isBareLayout) {
    // 로그인 등 → AppLayout(사이드바/헤더) 미적용
    return (
      <ConfigProvider locale={koKR}>
        <Component {...pageProps} />
      </ConfigProvider>
    );
  }

  return (
    <ConfigProvider locale={koKR}>
      <AppLayout>
        <Component {...pageProps} />
      </AppLayout>
    </ConfigProvider>
  );
}

export default wrapper.withRedux(MyApp);
