// components/AppLayout.js
import React from "react";
import PropTypes from "prop-types";
import { useRouter } from "next/router";
import { useDispatch, useSelector } from "react-redux";
import { Layout, Menu, Avatar, Dropdown } from "antd";
import {
  DatabaseOutlined, CheckSquareOutlined, UserOutlined, LogoutOutlined,
} from "@ant-design/icons";
import styled from "styled-components";
import { logoutRequest } from "../reducers/auth/authReducer";

const { Header, Sider, Content } = Layout;

const Logo = styled.div`
  height: 48px;
  margin: 12px;
  color: #fff;
  font-weight: 600;
  font-size: 15px;
  white-space: nowrap;
  overflow: hidden;
`;

const StyledHeader = styled(Header)`
  display: flex;
  align-items: center;
  justify-content: flex-end;
  background: #fff;
  padding: 0 20px;
  border-bottom: 1px solid #f0f0f0;
`;

// roles 는 이 메뉴를 보여줄 권한 화이트리스트다(비어있으면 전체 공개).
const MENU_ITEMS = [
  { key: "/datasets", label: "데이터셋", icon: <DatabaseOutlined />, roles: [] },
  { key: "/approvals", label: "승인 대기", icon: <CheckSquareOutlined />, roles: ["ROLE_ADMIN"] },
];

function AppLayout({ children }) {
  const router = useRouter();
  const dispatch = useDispatch();
  // state.auth 가 실제 상태 모양이다 - { user, initialized } (reducers/auth/authReducer.js, pages/index.js 참고).
  const { user } = useSelector((state) => state.auth);
  const roles = user?.roles || [];
  const isAdmin = roles.includes("ROLE_ADMIN");

  const visibleItems = MENU_ITEMS.filter(
    (item) => item.roles.length === 0 || (item.roles.includes("ROLE_ADMIN") && isAdmin),
  );

  const handleMenuClick = ({ key }) => {
    router.push(key);
  };

  const userMenu = (
    <Menu
      items={[
        { key: "logout", icon: <LogoutOutlined />, label: "로그아웃" },
      ]}
      onClick={({ key }) => {
        if (key === "logout") {
          dispatch(logoutRequest());
          router.push("/auth/login");
        }
      }}
    />
  );

  return (
    <Layout style={{ minHeight: "100vh" }}>
      <Sider breakpoint="lg" collapsedWidth="0">
        <Logo>AI 표준 도메인 추천</Logo>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[router.pathname]}
          items={visibleItems.map((item) => ({ key: item.key, icon: item.icon, label: item.label }))}
          onClick={handleMenuClick}
        />
      </Sider>
      <Layout>
        <StyledHeader>
          <Dropdown overlay={userMenu} placement="bottomRight">
            <span style={{ cursor: "pointer" }}>
              <Avatar size="small" icon={<UserOutlined />} style={{ marginRight: 8 }} />
              {user?.userName || "사용자"}
            </span>
          </Dropdown>
        </StyledHeader>
        <Content style={{ margin: 20 }}>{children}</Content>
      </Layout>
    </Layout>
  );
}

AppLayout.propTypes = {
  children: PropTypes.node.isRequired,
};

export default AppLayout;
