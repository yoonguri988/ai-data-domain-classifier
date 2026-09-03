import React, { useEffect, useState, useCallback } from "react";
import Head from "next/head";
import { useDispatch, useSelector } from "react-redux";
import {
  Card, Table, Tag, Button, Space, Tabs, message,
} from "antd";

const { TabPane } = Tabs;
import { fetchPendingRequest, reviewRequest } from "../../reducers/approval/approvalReducer";
import ApprovalActionModal from "../../components/common/ApprovalActionModal";
import api from "../../api/axios";

const STATUS_TAG = {
  PENDING: <Tag color="blue">대기</Tag>,
  APPROVED: <Tag color="green">승인</Tag>,
  REJECTED: <Tag color="red">반려</Tag>,
};

const BASE_COLUMNS = [
  { title: "신청 ID", dataIndex: "requestId", width: 80 },
  { title: "컬럼명", dataIndex: "columnName" },
  { title: "제안 도메인", dataIndex: "proposedDomainNameKo" },
  {
    title: "AI 추천 채택",
    dataIndex: "aiSuggestedYn",
    render: (v) => (v ? <Tag color="cyan">AI 추천</Tag> : <Tag>직접 선택</Tag>),
  },
  { title: "신청자", dataIndex: "requestedByName" },
  { title: "신청일시", dataIndex: "requestedAt" },
  { title: "상태", dataIndex: "requestStatus", render: (s) => STATUS_TAG[s] || s },
];

// 승인 대기(PENDING) 탭 - 기존 화면 그대로, Redux(approvalReducer/approvalSaga)로 처리한다.
function PendingTab() {
  const dispatch = useDispatch();
  const { pendingList, loading, reviewingIds } = useSelector((s) => s.approval);

  const [target, setTarget] = useState(null); // 처리할 신청 건 (StdDmnReqRsp)

  useEffect(() => {
    dispatch(fetchPendingRequest());
  }, [dispatch]);

  const handleSubmit = (payload) => {
    dispatch(reviewRequest(payload)); // { requestId, approve, rejectReason }
    setTarget(null);
  };

  const columns = [
    ...BASE_COLUMNS,
    {
      title: "처리",
      render: (_, record) => (
        <Space>
          <Button
            size="small"
            type="primary"
            loading={reviewingIds.includes(record.requestId)}
            onClick={() => setTarget(record)}
          >
            승인/반려
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <>
      <Table rowKey="requestId" loading={loading} columns={columns} dataSource={pendingList} />

      {target && (
        <ApprovalActionModal
          open={Boolean(target)}
          record={target}
          submitting={reviewingIds.includes(target.requestId)}
          onSubmit={handleSubmit}
          onCancel={() => setTarget(null)}
        />
      )}
    </>
  );
}

// 처리 이력 탭 - 승인자/관리자가 이미 승인/반려 처리한 건까지 전부 볼 수 있어야 한다는 요구사항 반영.
// 대기 목록(GET /api/std-domain-requests/pending)은 PENDING만 내려주므로, 이미 처리된 건은 전체 이력
// API(GET /api/std-domain-requests)로 따로 조회한다. 이 화면 전용의 읽기 전용 조회라 Redux 모듈을 새로
// 만들지 않고, pages/admin/users.js와 같은 방식(페이지 로컬 state + api.js 직접 호출)을 따랐다.
function HistoryTab() {
  const [historyList, setHistoryList] = useState([]);
  const [loading, setLoading] = useState(true);

  const loadHistory = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await api.get("/api/std-domain-requests");
      setHistoryList(data);
    } catch (error) {
      message.error(error.response?.data?.error || "처리 이력을 불러오지 못했습니다.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadHistory();
  }, [loadHistory]);

  const columns = [
    ...BASE_COLUMNS,
    { title: "처리자", dataIndex: "reviewedByName" },
    { title: "처리일시", dataIndex: "reviewedAt" },
    { title: "반려 사유", dataIndex: "rejectReason" },
  ];

  return (
    <Table
      rowKey="requestId"
      loading={loading}
      columns={columns}
      dataSource={historyList}
      pagination={{ pageSize: 10 }}
    />
  );
}

// antd Menu는 items 프롭(4.20+)을 이미 다른 화면(MegaMenu.js)에서 쓰고 있지만, Tabs의 items 프롭은
// 더 늦은 버전(4.23+)에서 추가됐다 - package.json이 "^4.8.6"로 넓게 잡혀 있어 정확히 어떤 4.x 패치가
// 설치돼 있는지 보장할 수 없으므로, 4.0부터 계속 지원되는 <Tabs.TabPane> children API를 대신 썼다
// (버전 차이로 화면에 아무것도 안 뜨는 문제를 또 만들지 않기 위함 - MegaMenu.js 초기 버전에서 겪었던
// 문제와 같은 종류다).
function ApprovalsPage() {
  return (
    <>
      <Head>
        <title>승인 대기 - AI 표준 도메인 추천</title>
      </Head>
      <Card title="표준 도메인 확정 승인">
        <Tabs defaultActiveKey="pending">
          <TabPane tab="승인 대기" key="pending">
            <PendingTab />
          </TabPane>
          <TabPane tab="처리 이력" key="history">
            <HistoryTab />
          </TabPane>
        </Tabs>
      </Card>
    </>
  );
}

export default ApprovalsPage;
