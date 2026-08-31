import React, { useState } from "react";
import PropTypes from "prop-types";
import { Modal, Form, Radio, Input } from "antd";

const { TextArea } = Input;

// StdDmnReqReviewReq { approve, rejectReason } 를 만들어 onSubmit 으로 넘겨주는 승인/반려 모달.
// approve=false 를 고르면 rejectReason 입력을 요구한다(백엔드도 Size(max=500)만 걸어두고 필수는
// 아니지만, 화면에서는 사유 없는 반려를 막는 게 실무적으로 맞다고 판단했다).
function ApprovalActionModal({ open, record = null, onSubmit, onCancel, submitting = false }) {
  const [approve, setApprove] = useState(true);
  const [rejectReason, setRejectReason] = useState("");

  const handleOk = () => {
    onSubmit({
      requestId: record.requestId,
      approve,
      rejectReason: approve ? undefined : rejectReason,
    });
  };

  return (
    <Modal
      title={`확정 신청 처리 - ${record?.columnName || ""}`}
      open={open}
      onOk={handleOk}
      onCancel={onCancel}
      confirmLoading={submitting}
      okText="처리"
      okButtonProps={{ disabled: !approve && !rejectReason.trim() }}
    >
      <p>
        제안 도메인:
        {" "}
        <strong>{record?.proposedDomainNameKo}</strong>
        {record?.aiSuggestedYn && " (AI 추천 채택)"}
      </p>
      <Form layout="vertical">
        <Form.Item label="처리 결과">
          <Radio.Group value={approve} onChange={(e) => setApprove(e.target.value)}>
            <Radio.Button value>승인</Radio.Button>
            <Radio.Button value={false}>반려</Radio.Button>
          </Radio.Group>
        </Form.Item>
        {!approve && (
          <Form.Item label="반려 사유" required>
            <TextArea
              rows={3}
              maxLength={500}
              value={rejectReason}
              onChange={(e) => setRejectReason(e.target.value)}
              placeholder="반려 사유를 입력하세요."
            />
          </Form.Item>
        )}
      </Form>
    </Modal>
  );
}

ApprovalActionModal.propTypes = {
  open: PropTypes.bool.isRequired,
  // eslint-disable-next-line react/forbid-prop-types
  record: PropTypes.object,
  onSubmit: PropTypes.func.isRequired,
  onCancel: PropTypes.func.isRequired,
  submitting: PropTypes.bool,
};

export default ApprovalActionModal;
