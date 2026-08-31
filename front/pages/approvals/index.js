import React, { useEffect, useState } from "react";
import Head from "next/head";
import { useDispatch, useSelector } from "react-redux";
import { Card, Table, Tag, Button, Space } from "antd";
import { fetchPendingRequest, reviewRequest } from "../../reducers/approval/approvalReducer";
import ApprovalActionModal from "../../components/common/ApprovalActionModal";

const STATUS_TAG = {
  PENDING: <Tag color="blue">대기</Tag>,
  APPROVED: <Tag color="green">승인</Tag>,
  REJECTED: <Tag color="red">반려</Tag>,
};

function ApprovalsPage() {
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
      <Head>
        <title>승인 대기 - AI 표준 도메인 추천</title>
      </Head>
      <Card title="승인 대기 목록">
        <Table rowKey="requestId" loading={loading} columns={columns} dataSource={pendingList} />
      </Card>

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

export default ApprovalsPage;
