// components/MegaMenu.js
import React from "react";
import { useRouter } from "next/router";
import { useSelector } from "react-redux";
import { Menu } from "antd";
import {
  DatabaseOutlined,
  CheckSquareOutlined,
  TagsOutlined,
  TeamOutlined,
} from "@ant-design/icons";
import styled from "styled-components";

const Logo = styled.div`
  height: 64px;
  display: flex;
  align-items: center;
  margin-right: 24px;
  color: #fff;
  font-weight: 600;
  font-size: 16px;
  white-space: nowrap;
`;

const Bar = styled.div`
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
`;

// 상단 가로 메가메뉴 - 로그인한 사용자의 역할(roles)에 따라 보이는 항목이 달라진다. 원래 좌측
// 세로 사이드바(Layout.Sider + Menu mode="inline")로 만들었었는데, "메가메뉴"는 관례적으로 상단 가로
// 내비게이션 + 그룹 항목에 마우스를 올리면 펼쳐지는 드롭다운 패널을 가리키는 이름이라 사이드바와는 다른
// 패턴이다 - 이름에 맞게 가로형으로 다시 짰다. antd Menu는 mode="horizontal"일 때 children이 있는
// 최상위 항목을 자동으로 호버 드롭다운으로 그려주므로(관리자 그룹) 별도 팝오버 없이 그대로 활용했다.
// 1) 데이터셋: 로그인한 사용자면 누구나(일반사용자/승인자/관리자 공통)
// 2) 승인 대기: 백엔드(StdDmnController, @PreAuthorize("hasAnyRole('ADMIN','REVIEWER')"))와 맞춰
//    ROLE_ADMIN 또는 ROLE_REVIEWER 에게만 - 시드 데이터의 reviewer1(김승인)이 바로 이 메뉴를 쓰는
//    승인 전담 계정이다.
// 3) 관리자(드롭다운: 공통코드 관리/사용자·권한 관리): ROLE_ADMIN 전용.
function MegaMenu() {
  const router = useRouter();
  const { user } = useSelector((state) => state.auth);
  const roles = user?.roles || [];
  const isAdmin = roles.includes("ROLE_ADMIN");
  const isReviewer = roles.includes("ROLE_REVIEWER");

  const items = [
    { key: "/datasets", icon: <DatabaseOutlined />, label: "데이터셋" },
  ];

  if (isAdmin || isReviewer) {
    items.push({
      key: "/approvals",
      icon: <CheckSquareOutlined />,
      label: "승인 대기",
    });
  }

  if (isAdmin) {
    items.push({
      key: "admin-group",
      label: "관리자",
      children: [
        {
          key: "/admin/common-codes",
          icon: <TagsOutlined />,
          label: "공통코드 관리",
        },
        {
          key: "/admin/users",
          icon: <TeamOutlined />,
          label: "사용자/권한 관리",
        },
      ],
    });
  }

  // 현재 경로가 하위 메뉴(예: /admin/users)에 속해 있으면 그 상위 드롭다운도 선택된 것처럼 표시되도록
  // antd가 자동으로 처리해주지만, 정확한 selectedKeys 매칭을 위해 현재 pathname 하나만 넘긴다.
  return (
    <Bar>
      <Logo>AI 표준 도메인 추천</Logo>
      <Menu
        theme="dark"
        mode="horizontal"
        selectedKeys={[router.pathname]}
        items={items}
        style={{ flex: 1, minWidth: 0, borderBottom: "none" }}
        onClick={({ key }) => {
          if (key !== "admin-group") router.push(key);
        }}
      />
    </Bar>
  );
}

export default MegaMenu;
