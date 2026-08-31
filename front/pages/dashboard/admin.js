import React from "react";
import Head from "next/head";
import { useSelector } from "react-redux";
import { Card, Typography, Space, Button } from "antd";
import { DatabaseOutlined, CheckSquareOutlined } from "@ant-design/icons";
import Link from "next/link";

function AdminDashboardPage() {
  const { user } = useSelector((s) => s.auth);

  return (
    <>
      <Head>
        <title>관리자 대시보드 - AI 표준 도메인 추천</title>
      </Head>
      <Card>
        <Typography.Title level={3}>
          안녕하세요,
          {" "}
          {user?.userName}
          님
        </Typography.Title>
        <Typography.Paragraph type="secondary">
          컬럼명을 등록하면 AI가 표준 도메인을 추천하고, 담당자가 확인 후 승인하면 표준으로 확정됩니다.
        </Typography.Paragraph>
        <Space>
          <Link href="/datasets" passHref>
            <Button type="primary" icon={<DatabaseOutlined />}>
              데이터셋 관리
            </Button>
          </Link>
          <Link href="/approvals" passHref>
            <Button icon={<CheckSquareOutlined />}>
              승인 대기 목록
            </Button>
          </Link>
        </Space>
      </Card>
    </>
  );
}

export default AdminDashboardPage;
