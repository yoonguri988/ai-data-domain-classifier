// components/Footer.js
import React from "react";
import { Layout } from "antd";
import styled from "styled-components";

const { Footer: AntFooter } = Layout;

const StyledFooter = styled(AntFooter)`
  text-align: center;
  color: #999;
  font-size: 12px;
  padding: 12px 20px;
`;

function Footer() {
  return (
    <StyledFooter>
      AI 표준 도메인 추천 · 포트폴리오 재구현 프로젝트
    </StyledFooter>
  );
}

export default Footer;
