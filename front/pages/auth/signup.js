import React, { useState } from "react";
import Head from "next/head";
import { useRouter } from "next/router";
import { Form, Input, Button, Card, Typography, Alert } from "antd";
import styled from "styled-components";
import api from "../../api/axios";

const Wrapper = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: #f0f2f5;
`;

// POST /auth/signup 은 saga/store 모듈 없이 이 화면에서만 쓰는 일회성 호출이라, 다른 화면의
// 데이터셋 상세(datasets/[datasetId]/index.js)와 같은 이유로 store 모듈을 따로 두지 않았다.
function SignupPage() {
  const router = useRouter();
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const handleFinish = async (values) => {
    setSubmitting(true);
    setError(null);
    try {
      // SignupReq { loginId, password, userName, email, phoneNo, deptName }
      await api.post("/auth/signup", values);
      router.replace("/auth/login");
    } catch (e) {
      setError(e.response?.data?.error || e.response?.data?.message || "회원가입에 실패했습니다.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <>
      <Head>
        <title>회원가입 - AI 표준 도메인 추천</title>
      </Head>
      <Wrapper>
        <Card style={{ width: 400 }}>
          <Typography.Title level={4} style={{ textAlign: "center", marginBottom: 24 }}>
            회원가입
          </Typography.Title>
          {error && (
            <Alert type="error" message={error} showIcon style={{ marginBottom: 16 }} />
          )}
          <Form layout="vertical" onFinish={handleFinish}>
            <Form.Item
              name="loginId"
              label="로그인 아이디"
              rules={[{ required: true, message: "로그인 아이디를 입력하세요." }]}
            >
              <Input placeholder="4~50자" autoComplete="username" />
            </Form.Item>
            <Form.Item
              name="password"
              label="비밀번호"
              rules={[{ required: true, message: "비밀번호를 입력하세요." }]}
            >
              <Input.Password placeholder="영문/숫자/특수문자 포함 8자 이상" autoComplete="new-password" />
            </Form.Item>
            <Form.Item
              name="userName"
              label="이름"
              rules={[{ required: true, message: "이름을 입력하세요." }]}
            >
              <Input />
            </Form.Item>
            <Form.Item
              name="email"
              label="이메일"
              rules={[
                { required: true, message: "이메일을 입력하세요." },
                { type: "email", message: "이메일 형식이 올바르지 않습니다." },
              ]}
            >
              <Input />
            </Form.Item>
            <Form.Item name="phoneNo" label="휴대폰 번호 (선택)">
              <Input placeholder="010-1234-5678" />
            </Form.Item>
            <Form.Item name="deptName" label="소속 부서명 (선택)">
              <Input />
            </Form.Item>
            <Form.Item>
              <Button type="primary" htmlType="submit" block loading={submitting}>
                가입하기
              </Button>
            </Form.Item>
          </Form>
          <div style={{ textAlign: "center" }}>
            <a onClick={() => router.push("/auth/login")} role="link" tabIndex={0}>
              로그인으로 돌아가기
            </a>
          </div>
        </Card>
      </Wrapper>
    </>
  );
}

export default SignupPage;
