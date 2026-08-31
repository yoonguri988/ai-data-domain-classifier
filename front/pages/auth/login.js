import React, { useEffect } from "react";
import Head from "next/head";
import { useRouter } from "next/router";
import { useDispatch, useSelector } from "react-redux";
import { Form, Input, Button, Card, Typography, Alert, Divider, Space } from "antd";
import { UserOutlined, LockOutlined } from "@ant-design/icons";
import styled from "styled-components";
import { loginRequest } from "../../reducers/auth/authReducer";

const Wrapper = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: #f0f2f5;
`;

const OAUTH_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080";
const OAUTH_PROVIDERS = [
  { key: "google", label: "Google로 로그인" },
  { key: "kakao", label: "Kakao로 로그인" },
  { key: "naver", label: "Naver로 로그인" },
];

function LoginPage() {
  const router = useRouter();
  const dispatch = useDispatch();
  const { user, loading, error } = useSelector((s) => s.auth);

  useEffect(() => {
    if (user) {
      router.replace("/");
    }
  }, [user, router]);

  const handleFinish = (values) => {
    dispatch(loginRequest(values)); // { loginId, password } = LoginReq
  };

  const handleOAuthClick = (provider) => {
    // Spring Security 가 이 경로를 가로채서 provider 로그인 화면으로 리다이렉트한다.
    window.location.href = `${OAUTH_BASE_URL}/oauth2/authorization/${provider}`;
  };

  return (
    <>
      <Head>
        <title>로그인 - AI 표준 도메인 추천</title>
      </Head>
      <Wrapper>
        <Card style={{ width: 360 }}>
          <Typography.Title level={4} style={{ textAlign: "center", marginBottom: 24 }}>
            AI 표준 도메인 추천
          </Typography.Title>
          {error && (
            <Alert type="error" message={error} showIcon style={{ marginBottom: 16 }} />
          )}
          <Form layout="vertical" onFinish={handleFinish}>
            <Form.Item
              name="loginId"
              label="아이디"
              rules={[{ required: true, message: "아이디를 입력하세요." }]}
            >
              <Input prefix={<UserOutlined />} autoComplete="username" />
            </Form.Item>
            <Form.Item
              name="password"
              label="비밀번호"
              rules={[{ required: true, message: "비밀번호를 입력하세요." }]}
            >
              <Input.Password prefix={<LockOutlined />} autoComplete="current-password" />
            </Form.Item>
            <Form.Item>
              <Button type="primary" htmlType="submit" block loading={loading}>
                로그인
              </Button>
            </Form.Item>
          </Form>

          <Divider plain>또는</Divider>
          <Space direction="vertical" style={{ width: "100%" }}>
            {OAUTH_PROVIDERS.map((p) => (
              <Button key={p.key} block onClick={() => handleOAuthClick(p.key)}>
                {p.label}
              </Button>
            ))}
          </Space>

          <div style={{ textAlign: "center", marginTop: 16 }}>
            계정이 없으신가요?
            {" "}
            <a onClick={() => router.push("/auth/signup")} role="link" tabIndex={0}>
              회원가입
            </a>
          </div>
        </Card>
      </Wrapper>
    </>
  );
}

export default LoginPage;
