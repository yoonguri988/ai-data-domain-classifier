import React, {
  useEffect, useState, useCallback,
} from "react";
import PropTypes from "prop-types";
import Head from "next/head";
import { useRouter } from "next/router";
import { useDispatch, useSelector } from "react-redux";
import {
  Card, Table, Button, Space, Modal, Form, Input, Switch, Select, message, Empty, Tag,
} from "antd";
import { PlusOutlined, FilePdfOutlined, RobotOutlined } from "@ant-design/icons";
import api from "../../../api/axios";
import DomainProbabilityBar from "../../../components/common/DomainProbabilityBar";
import { fetchPredictionsRequest, predictRequest } from "../../../reducers/prediction/predictionReducer";

// 컬럼 한 줄 + 그 컬럼의 AI 판별 결과(Top-N)를 함께 그리는 행 컴포넌트.
// prediction 리듀서의 상태는 columnId 별로 나뉘어 있어서(reducers/prediction/predictionReducer.js 참고),
// Table 의 expandable 대신 각 행을 별도 컴포넌트로 빼서 컬럼별로 독립적으로 useSelector 하게 했다.
//
// confirmedDomain: 이 컬럼에 이미 확정된 표준 도메인이 있으면 그 정보(StdDmnRsp), 없으면 null.
// GET /api/std-domains/columns/{columnId} 는 승인/반려 기능(StdDmnController)과 함께 처음부터 있었지만,
// 이 화면에서 아무도 호출하지 않아서 "확정 신청 → 승인자가 승인 → 신청자 화면에는 아무 표시도 안 남는"
// 상태였다(승인 여부는 /approvals 목록에서만 확인 가능했다). 신청한 사람 입장에서 자기 신청이 어떻게
// 됐는지 다시 여기로 돌아와서 볼 수 있어야 자연스러운 흐름이라 판단해 태그로 노출했다.
function ColumnPredictionRow({ column, onRequestConfirm, confirmedDomain }) {
  const dispatch = useDispatch();
  const predictions = useSelector((s) => s.prediction.byColumnId[column.columnId] || []);
  const loading = useSelector((s) => s.prediction.loadingColumnIds.includes(column.columnId));
  const predicting = useSelector((s) => s.prediction.predictingColumnIds.includes(column.columnId));

  useEffect(() => {
    dispatch(fetchPredictionsRequest(column.columnId));
  }, [dispatch, column.columnId]);

  return (
    <Card
      type="inner"
      size="small"
      title={(
        <Space>
          {`${column.columnName}${column.columnNameKo ? ` (${column.columnNameKo})` : ""}`}
          {confirmedDomain && (
            <Tag color="green">
              {`확정: ${confirmedDomain.domainNameKo} (v${confirmedDomain.versionNo})`}
            </Tag>
          )}
        </Space>
      )}
      style={{ marginBottom: 12 }}
      extra={(
        <Space>
          <Button
            size="small"
            icon={<RobotOutlined />}
            loading={predicting}
            onClick={() => dispatch(predictRequest(column.columnId))}
          >
            AI 판별
          </Button>
          <Button
            size="small"
            type="primary"
            disabled={predictions.length === 0}
            onClick={() => onRequestConfirm(column, predictions)}
          >
            확정 신청
          </Button>
        </Space>
      )}
      loading={loading}
    >
      {predictions.length === 0 ? (
        <Empty
          image={Empty.PRESENTED_IMAGE_SIMPLE}
          description="아직 AI 판별 결과가 없습니다. 'AI 판별' 버튼을 눌러보세요."
        />
      ) : (
        predictions.map((p) => (
          <DomainProbabilityBar
            key={p.predictionId}
            rank={p.predictionRank}
            domainNameKo={p.domainNameKo}
            probability={Number(p.probability)}
            cacheHitYn={p.cacheHitYn}
          />
        ))
      )}
    </Card>
  );
}

ColumnPredictionRow.propTypes = {
  // eslint-disable-next-line react/forbid-prop-types
  column: PropTypes.object.isRequired,
  onRequestConfirm: PropTypes.func.isRequired,
  // eslint-disable-next-line react/forbid-prop-types
  confirmedDomain: PropTypes.object,
};

ColumnPredictionRow.defaultProps = {
  confirmedDomain: null,
};

function DatasetDetailPage() {
  const router = useRouter();
  const { datasetId } = router.query;

  const [dataset, setDataset] = useState(null);
  const [columns, setColumns] = useState([]);
  const [domains, setDomains] = useState([]);
  const [confirmedByColumnId, setConfirmedByColumnId] = useState({});
  const [loadingPage, setLoadingPage] = useState(true);

  const [columnModalOpen, setColumnModalOpen] = useState(false);
  const [columnForm] = Form.useForm();

  const [confirmTarget, setConfirmTarget] = useState(null); // { column, predictions }
  const [confirmForm] = Form.useForm();
  const [confirmSubmitting, setConfirmSubmitting] = useState(false);

  // 데이터셋 상세/컬럼 목록/도메인 후보는 이 화면에서만 쓰는 읽기 전용 데이터라, 별도 redux 모듈 없이
  // 페이지 로컬 상태로 관리한다(공유 상태가 필요한 예측/승인만 reducers/sagas 모듈로 분리했다).
  const loadPage = useCallback(async () => {
    if (!datasetId) return;
    setLoadingPage(true);
    try {
      const [dsetRes, colsRes, domainsRes] = await Promise.all([
        api.get(`/api/datasets/${datasetId}`),
        api.get(`/api/datasets/${datasetId}/columns`),
        api.get("/api/domains"),
      ]);
      setDataset(dsetRes.data);
      setColumns(colsRes.data);
      setDomains(domainsRes.data);

      // 컬럼별 확정 여부는 컬럼 목록을 받아온 뒤에야 columnId를 알 수 있어서 별도로 이어서 불러온다.
      // 확정 안 된 컬럼은 404가 정상 응답이라, 그 경우만 조용히 null 처리하고 나머지 에러는 그대로 띄운다.
      const confirmedEntries = await Promise.all(
        colsRes.data.map(async (col) => {
          try {
            const { data } = await api.get(`/api/std-domains/columns/${col.columnId}`);
            return [col.columnId, data];
          } catch (error) {
            if (error.response?.status === 404) return [col.columnId, null];
            throw error;
          }
        }),
      );
      setConfirmedByColumnId(Object.fromEntries(confirmedEntries));
    } catch (error) {
      message.error(error.response?.data?.error || "데이터셋 정보를 불러오지 못했습니다.");
    } finally {
      setLoadingPage(false);
    }
  }, [datasetId]);

  useEffect(() => {
    loadPage();
  }, [loadPage]);

  const handleAddColumn = async (values) => {
    try {
      await api.post("/api/datasets/columns", {
        datasetId,
        columns: [values], // AnlColBulkCreateReq - 화면에서는 한 번에 1건씩만 등록한다
      });
      setColumnModalOpen(false);
      columnForm.resetFields();
      message.success("컬럼을 등록했습니다.");
      loadPage();
    } catch (error) {
      message.error(error.response?.data?.error || "컬럼 등록에 실패했습니다.");
    }
  };

  const openConfirmModal = (column, predictions) => {
    const topPick = predictions[0];
    confirmForm.setFieldsValue({ proposedDomainCode: topPick?.domainCode });
    setConfirmTarget({ column, predictions });
  };

  const handleConfirmRequest = async (values) => {
    setConfirmSubmitting(true);
    try {
      const topPick = confirmTarget.predictions[0];
      await api.post("/api/std-domain-requests", {
        columnId: confirmTarget.column.columnId,
        proposedDomainCode: values.proposedDomainCode,
        aiSuggestedYn: Boolean(topPick && topPick.domainCode === values.proposedDomainCode),
      });
      message.success("표준 도메인 확정을 신청했습니다. 승인권자의 검토를 기다려주세요.");
      setConfirmTarget(null);
    } catch (error) {
      message.error(error.response?.data?.error || "확정 신청에 실패했습니다.");
    } finally {
      setConfirmSubmitting(false);
    }
  };

  const handleDownloadReport = async () => {
    try {
      const response = await api.get(`/api/datasets/${datasetId}/report.pdf`, { responseType: "blob" });
      // Authorization 헤더가 필요해서 <a href> 로 바로 못 받고, Blob 으로 받아 임시 링크를 만들어 내려받는다.
      const url = window.URL.createObjectURL(new Blob([response.data], { type: "application/pdf" }));
      const link = document.createElement("a");
      link.href = url;
      link.download = `${datasetId}_report.pdf`;
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (error) {
      message.error("PDF 리포트 다운로드에 실패했습니다.");
    }
  };

  return (
    <>
      <Head>
        <title>{dataset ? dataset.datasetName : "데이터셋 상세"}</title>
      </Head>
      <Card
        loading={loadingPage}
        title={dataset ? `${dataset.datasetName} (${dataset.dbSchemaName}.${dataset.tableName})` : ""}
        extra={(
          <Space>
            <Button icon={<FilePdfOutlined />} onClick={handleDownloadReport}>
              PDF 리포트
            </Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={() => setColumnModalOpen(true)}>
              컬럼 등록
            </Button>
          </Space>
        )}
      >
        {columns.length === 0 && !loadingPage ? (
          <Empty description="등록된 컬럼이 없습니다. '컬럼 등록'으로 먼저 컬럼 메타를 추가하세요." />
        ) : (
          columns.map((column) => (
            <ColumnPredictionRow
              key={column.columnId}
              column={column}
              onRequestConfirm={openConfirmModal}
              confirmedDomain={confirmedByColumnId[column.columnId]}
            />
          ))
        )}
      </Card>

      <Modal
        title="컬럼 등록"
        open={columnModalOpen}
        onCancel={() => setColumnModalOpen(false)}
        onOk={() => columnForm.submit()}
        okText="등록"
      >
        <Form
          form={columnForm}
          layout="vertical"
          onFinish={handleAddColumn}
          initialValues={{ numericYn: false, dateYn: false, uniqueYn: false }}
        >
          <Form.Item
            name="columnName"
            label="컬럼명"
            rules={[{ required: true, message: "컬럼명을 입력하세요." }]}
          >
            <Input placeholder="예: CUST_PHONE_NO" />
          </Form.Item>
          <Form.Item name="columnNameKo" label="컬럼 한글명">
            <Input placeholder="예: 고객연락처" />
          </Form.Item>
          <Form.Item name="dataType" label="DB 데이터 타입">
            <Input placeholder="예: VARCHAR2" />
          </Form.Item>
          <Space size="large">
            <Form.Item name="numericYn" label="숫자형" valuePropName="checked">
              <Switch />
            </Form.Item>
            <Form.Item name="dateYn" label="날짜형" valuePropName="checked">
              <Switch />
            </Form.Item>
            <Form.Item name="uniqueYn" label="유니크" valuePropName="checked">
              <Switch />
            </Form.Item>
          </Space>
        </Form>
      </Modal>

      <Modal
        title={confirmTarget ? `표준 도메인 확정 신청 - ${confirmTarget.column.columnName}` : ""}
        open={Boolean(confirmTarget)}
        onCancel={() => setConfirmTarget(null)}
        onOk={() => confirmForm.submit()}
        confirmLoading={confirmSubmitting}
        okText="신청"
      >
        <Form form={confirmForm} layout="vertical" onFinish={handleConfirmRequest}>
          <Form.Item
            name="proposedDomainCode"
            label="제안 도메인"
            rules={[{ required: true, message: "도메인을 선택하세요." }]}
          >
            <Select
              options={domains.map((d) => ({ value: d.domainCode, label: d.domainNameKo }))}
              placeholder="AI 판별 1순위가 기본 선택되어 있습니다"
            />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}

export default DatasetDetailPage;
