// components/AppLayout.js
import React from "react";
import PropTypes from "prop-types";
import { Layout } from "antd";
import Header from "./Header";
import MegaMenu from "./MegaMenu";
import Footer from "./Footer";

const { Header: AntHeader, Content } = Layout;

// 화면 전체 뼈대만 조립한다 - 실제 내용(로고/가로 메뉴, 사용자 정보/로그아웃, 하단 안내문)은 각각
// MegaMenu.js / Header.js / Footer.js 로 분리했다.
//
// MegaMenu(가로 메뉴)와 Header(우측 사용자 영역)는 하나의 상단 바(Layout.Header) 안에 나란히 놓인다 -
// 이전 버전은 좌측 세로 사이드바(Sider)로 만들었었는데, "메가메뉴"라는 이름 자체가 관례적으로 상단
// 가로 내비게이션 + 호버 드롭다운을 가리키는 패턴이라 이름에 맞게 가로형으로 다시 짰다. 그래서 각각
// 자체적으로 Layout.Header/Sider를 감싸지 않고, 이 파일이 갖고 있는 단 하나의 Layout.Header 안에
// 플렉스로 나란히 배치한다(둘 다 자체 래퍼를 가지면 상단 바가 두 겹으로 겹친다).
function AppLayout({ children }) {
  return (
    <Layout style={{ minHeight: "100vh" }}>
      <AntHeader
        style={{ display: "flex", alignItems: "center", padding: "0 20px" }}
      >
        <MegaMenu />
        <Header />
      </AntHeader>
      <Content style={{ margin: 20 }}>{children}</Content>
      <Footer />
    </Layout>
  );
}

AppLayout.propTypes = {
  children: PropTypes.node.isRequired,
};

export default AppLayout;
