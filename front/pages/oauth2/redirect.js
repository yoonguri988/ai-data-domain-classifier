import React, { useEffect } from "react";
import Head from "next/head";
import { useRouter } from "next/router";
import { useDispatch } from "react-redux";
import { Spin } from "antd";
import { loadUserRequest } from "../../reducers/auth/authReducer";

// Google/Kakao/Naver OAuth2 로그인 성공 시 OAuth2SuccessHandler(백엔드)가 브라우저를
// `${app.oauth2.redirect-url}?accessToken=...` 로 리다이렉트한다(리다이렉트는 응답 바디를 못 실어서
// 쿼리스트링으로 accessToken만 넘긴다). accessToken을 쿠키가 아니라 Redux(메모리)에만 두는 방식으로
// 바꾸면서(api/axios.js 참고 - 더 안전한 저장 방식), 여기서 받은 쿼리스트링 값은 "로그인이 성공했다"는
// 신호로만 쓰고 직접 저장하지는 않는다. refreshToken 쿠키는 이 리다이렉트 응답에 OAuth2SuccessHandler가
// 이미 HttpOnly로 심어놨으므로, 일반 로그인/새로고침과 똑같이 loadUserRequest(POST /auth/reissue)를
// 한 번 불러서 accessToken + 사용자 정보를 다시 받아오면 별도의 처리 경로를 만들지 않아도 된다.
function OAuth2RedirectPage() {
  const router = useRouter();
  const dispatch = useDispatch();

  useEffect(() => {
    if (!router.isReady) return;

    if (!router.query.accessToken) {
      router.replace("/auth/login");
      return;
    }

    dispatch(loadUserRequest());
    // 요구사항: 소셜 로그인 후에는 "/"(역할별 분기 라우터, pages/index.js)를 거치지 않고 바로
    // /dashboard/member 로 이동한다. 관리자 계정으로 소셜 로그인한 경우에도 동일하며, 관리자 전용
    // 화면은 상단 메가메뉴(MegaMenu.js)에서 계속 접근할 수 있다.
    router.replace("/dashboard/member");
  }, [router, dispatch]);

  return (
    <>
      <Head>
        <title>로그인 처리 중 - AI 표준 도메인 추천</title>
      </Head>
      <div style={{ display: "flex", justifyContent: "center", alignItems: "center", height: "100vh" }}>
        <Spin size="large" />
      </div>
    </>
  );
}

export default OAuth2RedirectPage;
