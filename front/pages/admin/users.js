import React, { useEffect, useState, useCallback } from "react";
import Head from "next/head";
import { useRouter } from "next/router";
import { useSelector } from "react-redux";
import {
  Card, Table, Select, Tag, message,
} from "antd";
import api from "../../api/axios";

const ROLE_COLOR = {
  ROLE_ADMIN: "red",
  ROLE_REVIEWER: "gold",
  ROLE_USER: "blue",
};

// 회원가입은 항상 ROLE_USER만 자동 부여한다(AuthAcntService.grantDefaultRole 참고) - 그 위 단계
// (ROLE_REVIEWER로 승격시켜 승인 업무를 맡기거나, ROLE_ADMIN을 추가로 주는 일)는 DDL 시드 데이터로
// 미리 넣어둔 admin/reviewer1 두 계정 말고는 할 방법이 없었다. 이 화면이 그 빈 자리를 채운다 - 새로
// 가입한 사용자도 관리자가 여기서 바로 역할을 부여/회수할 수 있다(GET /auth/users, GET /auth/roles,
// PATCH /auth/users/{userId}/roles 모두 ROLE_ADMIN 전용).
function UsersPage() {
  const router = useRouter();
  const { user, initialized } = useSelector((s) => s.auth);
  const isAdmin = (user?.roles || []).includes("ROLE_ADMIN");

  const [users, setUsers] = useState([]);
  const [roleOptions, setRoleOptions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [savingUserId, setSavingUserId] = useState(null);

  useEffect(() => {
    if (!initialized) return;
    if (!isAdmin) {
      router.replace("/");
    }
  }, [initialized, isAdmin, router]);

  const loadAll = useCallback(async () => {
    setLoading(true);
    try {
      const [usersRes, rolesRes] = await Promise.all([
        api.get("/auth/users"),
        api.get("/auth/roles"),
      ]);
      setUsers(usersRes.data);
      setRoleOptions(rolesRes.data.map((r) => ({ value: r.roleCode, label: r.roleName })));
    } catch (error) {
      message.error(error.response?.data?.error || "사용자 목록을 불러오지 못했습니다.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (isAdmin) loadAll();
  }, [isAdmin, loadAll]);

  const handleRolesChange = async (targetUser, newRoles) => {
    // 관리자 본인이 실수로 자기 자신의 ROLE_ADMIN을 빼면 다시 이 화면에 못 들어올 수 있다 - 그것만 막는다.
    if (
      targetUser.userId === user.userId
      && targetUser.roles.includes("ROLE_ADMIN")
      && !newRoles.includes("ROLE_ADMIN")
    ) {
      message.warning("본인의 관리자 권한은 이 화면에서 스스로 회수할 수 없습니다.");
      return;
    }

    const added = newRoles.filter((r) => !targetUser.roles.includes(r));
    const removed = targetUser.roles.filter((r) => !newRoles.includes(r));
    if (added.length === 0 && removed.length === 0) return;

    setSavingUserId(targetUser.userId);
    try {
      await Promise.all([
        ...added.map((roleCode) => api.patch(`/auth/users/${targetUser.userId}/roles`, { roleCode, grant: true })),
        ...removed.map((roleCode) => api.patch(`/auth/users/${targetUser.userId}/roles`, { roleCode, grant: false })),
      ]);
      message.success(`${targetUser.userName}님의 권한을 변경했습니다.`);
      await loadAll();
    } catch (error) {
      message.error(error.response?.data?.error || "권한 변경에 실패했습니다.");
    } finally {
      setSavingUserId(null);
    }
  };

  if (!isAdmin) {
    return null;
  }

  const columns = [
    { title: "사용자 ID", dataIndex: "userId", width: 90 },
    { title: "로그인 아이디", dataIndex: "loginId" },
    { title: "이름", dataIndex: "userName" },
    { title: "이메일", dataIndex: "email" },
    { title: "부서", dataIndex: "deptName" },
    {
      title: "상태",
      dataIndex: "userStatus",
      width: 90,
      render: (status) => <Tag color={status === "ACTIVE" ? "green" : "default"}>{status}</Tag>,
    },
    {
      title: "권한",
      dataIndex: "roles",
      width: 320,
      render: (roles, record) => (
        <Select
          mode="multiple"
          style={{ width: "100%" }}
          value={roles}
          options={roleOptions}
          loading={savingUserId === record.userId}
          disabled={savingUserId === record.userId}
          tagRender={(props) => (
            // eslint-disable-next-line react/prop-types
            <Tag color={ROLE_COLOR[props.value] || "default"} closable={props.closable} onClose={props.onClose}>
              {/* eslint-disable-next-line react/prop-types */}
              {props.label}
            </Tag>
          )}
          onChange={(newRoles) => handleRolesChange(record, newRoles)}
        />
      ),
    },
  ];

  return (
    <>
      <Head>
        <title>사용자/권한 관리 - AI 표준 도메인 추천</title>
      </Head>
      <Card title="사용자/권한 관리">
        <Table rowKey="userId" loading={loading} columns={columns} dataSource={users} pagination={{ pageSize: 10 }} />
      </Card>
    </>
  );
}

export default UsersPage;
