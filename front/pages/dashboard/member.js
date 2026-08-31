import React from "react";
import Head from "next/head";
import { useSelector } from "react-redux";
import { Card, Typography, Space, Button } from "antd";
import { DatabaseOutlined } from "@ant-design/icons";
import Link from "next/link";

// ROLE_ADMIN이 아닌 일반 사원용 대시보드. "승인 대기"는 관리자 전용 화면이라 안내하지 않는다
// (메뉴에서도 components/AppLayout.js가 ROLE_ADMIN이 아니면 숨긴다).
function MemberDashboardPage() {
  const { user } = useSelector((s) => s.auth);

  return (
    <>
      <Head>
        <title>대시보드 - AI 표준 도메인 추천</title>
      </Head>
      <Card>
        <Typography.Title level={3}>
          안녕하세요,
          {" "}
          {user?.userName}
          님
        </Typography.Title>
        <Typography.Paragraph type="secondary">
          컬럼명을 등록하면 AI가 표준 도메인을 추천합니다. 확정을 신청하면 관리자 승인 후 표준으로 등록됩니다.
        </Typography.Paragraph>
        <Space>
          <Link href="/datasets" passHref>
            <Button type="primary" icon={<DatabaseOutlined />}>
              데이터셋 관리
            </Button>
          </Link>
        </Space>
      </Card>
    </>
  );
}

export default MemberDashboardPage;
