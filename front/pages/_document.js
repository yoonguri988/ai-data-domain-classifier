import React from "react";
import Document, { Html, Head, Main, NextScript } from "next/document";
import { ServerStyleSheet } from "styled-components";

// styled-components 를 SSR 할 때 필요한 표준 보일러플레이트 - .babelrc 의
// babel-plugin-styled-components(ssr:true)와 짝을 이룬다. 서버에서 렌더링하며 모은 스타일을
// <head> 에 심어줘야 첫 화면부터 스타일이 적용된 채로 내려간다(FOUC 방지).
export default class MyDocument extends Document {
  static async getInitialProps(ctx) {
    const sheet = new ServerStyleSheet();
    const originalRenderPage = ctx.renderPage;

    try {
      ctx.renderPage = () => originalRenderPage({
        enhanceApp: (App) => function EnhancedApp(props) {
          return sheet.collectStyles(<App {...props} />);
        },
      });

      const initialProps = await Document.getInitialProps(ctx);
      return {
        ...initialProps,
        styles: (
          <>
            {initialProps.styles}
            {sheet.getStyleElement()}
          </>
        ),
      };
    } finally {
      sheet.seal();
    }
  }

  render() {
    return (
      <Html lang="ko">
        <Head />
        <body>
          <Main />
          <NextScript />
        </body>
      </Html>
    );
  }
}
