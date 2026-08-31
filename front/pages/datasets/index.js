import React, { useEffect, useState } from "react";
import Head from "next/head";
import Link from "next/link";
import { useDispatch, useSelector } from "react-redux";
import {
  Card, Table, Button, Tag, Modal, Form, Input, message,
} from "antd";
import { PlusOutlined } from "@ant-design/icons";
import { fetchMyDatasetsRequest, createDatasetRequest } from "../../reducers/dataset/datasetReducer";

const STATUS_COLOR = {
  REGISTERED: "default",
  ANALYZING: "processing",
  ANALYZED: "success",
};

function DatasetsPage() {
  const dispatch = useDispatch();
  const {
    list, loading, creating, createError,
  } = useSelector((s) => s.dataset);

  const [modalOpen, setModalOpen] = useState(false);
  const [form] = Form.useForm();

  useEffect(() => {
    dispatch(fetchMyDatasetsRequest());
  }, [dispatch]);

  useEffect(() => {
    if (createError) {
      message.error(createError);
    }
  }, [createError]);

  const handleCreate = (values) => {
    // 성공/실패는 여기서 바로 알 수 없다(saga가 비동기로 처리) - 실패하면 위 useEffect가 메시지를
    // 띄우고, 성공하면 createDatasetSuccess가 목록 맨 앞에 새 항목을 넣어준다.
    dispatch(createDatasetRequest(values)); // AnlDsetCreateReq
    setModalOpen(false);
    form.resetFields();
  };

  const columns = [
    { title: "데이터셋 ID", dataIndex: "datasetId" },
    {
      title: "데이터셋명",
      dataIndex: "datasetName",
      render: (text, record) => (
        <Link href={`/datasets/${record.datasetId}`}>{text}</Link>
      ),
    },
    {
      title: "대상 테이블",
      render: (_, record) => `${record.dbSchemaName}.${record.tableName}`,
    },
    { title: "DBMS", dataIndex: "dbmsTypeCode" },
    {
      title: "상태",
      dataIndex: "datasetStatus",
      render: (status) => <Tag color={STATUS_COLOR[status] || "default"}>{status}</Tag>,
    },
    { title: "등록자", dataIndex: "requestedByName" },
  ];

  return (
    <>
      <Head>
        <title>데이터셋 - AI 표준 도메인 추천</title>
      </Head>
      <Card
        title="내 데이터셋"
        extra={(
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setModalOpen(true)}>
            데이터셋 등록
          </Button>
        )}
      >
        <Table
          rowKey="datasetId"
          loading={loading}
          columns={columns}
          dataSource={list}
          pagination={{ pageSize: 10 }}
        />
      </Card>

      <Modal
        title="데이터셋 등록"
        open={modalOpen}
        onCancel={() => setModalOpen(false)}
        onOk={() => form.submit()}
        confirmLoading={creating}
        okText="등록"
      >
        <Form form={form} layout="vertical" onFinish={handleCreate}>
          <Form.Item
            name="datasetName"
            label="데이터셋명"
            rules={[{ required: true, message: "데이터셋명을 입력하세요." }]}
          >
            <Input placeholder="예: 고객 마스터 테이블" />
          </Form.Item>
          <Form.Item
            name="dbSchemaName"
            label="DB 스키마명"
            rules={[{ required: true, message: "DB 스키마명을 입력하세요." }]}
          >
            <Input placeholder="예: SALES" />
          </Form.Item>
          <Form.Item
            name="tableName"
            label="테이블명"
            rules={[{ required: true, message: "테이블명을 입력하세요." }]}
          >
            <Input placeholder="예: TB_CUSTOMER" />
          </Form.Item>
          <Form.Item name="dbmsTypeCode" label="DBMS 종류 (선택, 기본값: ORA)">
            <Input placeholder="예: ORA" />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}

export default DatasetsPage;
