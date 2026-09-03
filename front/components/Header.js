// components/Header.js
import React from "react";
import { useDispatch, useSelector } from "react-redux";
import { Dropdown, Avatar, Menu, Tag } from "antd";
import { UserOutlined, LogoutOutlined } from "@ant-design/icons";
import styled from "styled-components";
import { logoutRequest } from "../reducers/auth/authReducer";

// MegaMenu(가로 메뉴)와 같은 상단 바에 나란히 놓이는 우측 사용자 영역이라, 더 이상 자체적으로
// Layout.Header를 감싸지 않는다(그러면 상단 바가 두 겹으로 생긴다) - AppLayout.js가 MegaMenu와 이
// 컴포넌트를 하나의 Layout.Header 안에 같이 배치한다. 배경이 어두운 바(antd Layout.Header 기본색)
// 위에 놓이므로 글자색을 흰색 계열로 맞췄다.
const UserArea = styled.span`
  display: flex;
  align-items: center;
  flex-shrink: 0;
  margin-left: 16px;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.85);
  white-space: nowrap;

  &:hover {
    color: #fff;
  }
`;

// 역할 코드 → 화면에 보여줄 뱃지 문구/색. 한 사용자가 여러 역할을 가질 수도 있어서(예: 관리자이면서
// 동시에 ROLE_USER), 배열 순서대로 가장 먼저 매칭되는 역할 하나만 대표로 보여준다 - 로그인 중인 계정이
// admin/reviewer1/일반 가입자 중 누구인지 화면만 보고 바로 구분할 수 있게 하려는 목적이다(시연할 때
// 특히 유용하다).
const ROLE_BADGES = [
  { role: "ROLE_ADMIN", label: "관리자", color: "red" },
  { role: "ROLE_REVIEWER", label: "승인자", color: "gold" },
  { role: "ROLE_USER", label: "일반사용자", color: "blue" },
];

function Header() {
  const dispatch = useDispatch();
  const { user } = useSelector((state) => state.auth);
  const roles = user?.roles || [];
  const badge = ROLE_BADGES.find((b) => roles.includes(b.role));

  const userMenu = (
    <Menu
      items={[
        { key: "logout", icon: <LogoutOutlined />, label: "로그아웃" },
      ]}
      onClick={({ key }) => {
        // /auth/login 으로의 이동은 여기서 하지 않는다 - logoutRequest()는 비동기 saga(logoutSaga)라
        // 아직 로그아웃이 끝나기 전에 이동시키면 로그인 페이지가 stale한 user 값을 보고 다시 "/"로
        // 튕겨내는 경쟁 상태가 있었다. logoutSaga가 logoutDone() 이후 하드 리다이렉트로 직접 이동시킨다
        // (sagas/auth/authSaga.js 참고).
        if (key === "logout") {
          dispatch(logoutRequest());
        }
      }}
    />
  );

  return (
    <Dropdown overlay={userMenu} placement="bottomRight">
      <UserArea>
        {badge && (
          <Tag color={badge.color} style={{ marginRight: 8 }}>
            {badge.label}
          </Tag>
        )}
        <Avatar size="small" icon={<UserOutlined />} style={{ marginRight: 8 }} />
        {user?.userName || "사용자"}
      </UserArea>
    </Dropdown>
  );
}

export default Header;
