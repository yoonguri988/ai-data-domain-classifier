import React, { useEffect, useState, useCallback } from "react";
import Head from "next/head";
import { useRouter } from "next/router";
import { useSelector } from "react-redux";
import {
  Row, Col, Card, List, Button, Table, Switch, Modal, Form, Input, InputNumber, message, Empty,
} from "antd";
import { PlusOutlined } from "@ant-design/icons";
import api from "../../api/axios";

// 공통코드 그룹/상세 CRUD 화면. 백엔드 CmnCdController 는 처음부터 ROLE_ADMIN 전용 등록/수정(upsert)
// API를 갖고 있었는데(GET /api/common-codes/**는 비로그인도 조회 가능), 그동안 이 화면이 없어서
// DDL의 DML(시드 데이터)로만 공통코드가 채워져 있었다. 여러 화면이 공유하는 상태가 아니라 이 화면
// 전용 CRUD라, datasets/[datasetId]/index.js와 같은 이유로 별도 reducer/saga 없이 페이지 로컬
// 상태 + api/axios.js 직접 호출로 짰다.
function CommonCodesPage() {
  const router = useRouter();
  const { user, initialized } = useSelector((s) => s.auth);
  const isAdmin = (user?.roles || []).includes("ROLE_ADMIN");

  const [groups, setGroups] = useState([]);
  const [selectedGroup, setSelectedGroup] = useState(null);
  const [codes, setCodes] = useState([]);
  const [loadingGroups, setLoadingGroups] = useState(true);
  const [loadingCodes, setLoadingCodes] = useState(false);

  const [groupModalOpen, setGroupModalOpen] = useState(false);
  const [groupForm] = Form.useForm();

  const [codeModalOpen, setCodeModalOpen] = useState(false);
  const [codeForm] = Form.useForm();
  const [editingCode, setEditingCode] = useState(null); // null이면 신규 등록

  // ROLE_ADMIN이 아니면 접근할 수 없다 - 세션 복원(initialized)이 끝나기 전에는 아직 판단하지 않는다.
  useEffect(() => {
    if (!initialized) return;
    if (!isAdmin) {
      router.replace("/");
    }
  }, [initialized, isAdmin, router]);

  const loadGroups = useCallback(async () => {
    setLoadingGroups(true);
    try {
      const { data } = await api.get("/api/common-codes/groups");
      setGroups(data);
    } catch (error) {
      message.error(error.response?.data?.error || "코드 그룹 목록을 불러오지 못했습니다.");
    } finally {
      setLoadingGroups(false);
    }
  }, []);

  const loadCodes = useCallback(async (codeGroup) => {
    if (!codeGroup) return;
    setLoadingCodes(true);
    try {
      // useOnly=false 로 사용중지(useYn=false)인 코드까지 전부 봐야 관리자가 다시 켤 수 있다.
      const { data } = await api.get("/api/common-codes", { params: { codeGroup, useOnly: false } });
      setCodes(data);
    } catch (error) {
      message.error(error.response?.data?.error || "공통코드 목록을 불러오지 못했습니다.");
    } finally {
      setLoadingCodes(false);
    }
  }, []);

  useEffect(() => {
    if (isAdmin) loadGroups();
  }, [isAdmin, loadGroups]);

  const handleSelectGroup = (codeGroup) => {
    setSelectedGroup(codeGroup);
    loadCodes(codeGroup);
  };

  const handleAddGroup = async (values) => {
    try {
      await api.post("/api/common-codes/groups", values);
      message.success("코드 그룹을 등록했습니다.");
      setGroupModalOpen(false);
      groupForm.resetFields();
      loadGroups();
    } catch (error) {
      message.error(error.response?.data?.error || "코드 그룹 등록에 실패했습니다.");
    }
  };

  const openAddCodeModal = () => {
    setEditingCode(null);
    codeForm.resetFields();
    codeForm.setFieldsValue({ codeGroup: selectedGroup, sortOrder: (codes.length + 1) * 10, useYn: true });
    setCodeModalOpen(true);
  };

  const openEditCodeModal = (record) => {
    setEditingCode(record);
    codeForm.setFieldsValue(record);
    setCodeModalOpen(true);
  };

  const handleSaveCode = async (values) => {
    try {
      // 등록/수정 모두 같은 upsert API - (codeGroup, codeValue) 가 이미 있으면 나머지 값만 갱신된다.
      await api.post("/api/common-codes", values);
      message.success(editingCode ? "코드를 수정했습니다." : "코드를 등록했습니다.");
      setCodeModalOpen(false);
      loadCodes(selectedGroup);
    } catch (error) {
      message.error(error.response?.data?.error || "코드 저장에 실패했습니다.");
    }
  };

  const handleToggleUseYn = async (record, checked) => {
    try {
      await api.post("/api/common-codes", { ...record, useYn: checked });
      setCodes((prev) => prev.map((c) => (
        c.codeValue === record.codeValue ? { ...c, useYn: checked } : c
      )));
    } catch (error) {
      message.error(error.response?.data?.error || "사용 여부 변경에 실패했습니다.");
    }
  };

  if (!isAdmin) {
    return null; // "/" 로 리다이렉트되는 순간까지 잠깐 빈 화면
  }

  const columns = [
    { title: "코드값", dataIndex: "codeValue" },
    { title: "코드명", dataIndex: "codeName" },
    { title: "정렬순서", dataIndex: "sortOrder", width: 90 },
    {
      title: "사용여부",
      dataIndex: "useYn",
      width: 90,
      render: (useYn, record) => (
        <Switch checked={useYn} onChange={(checked) => handleToggleUseYn(record, checked)} />
      ),
    },
    {
      title: "",
      width: 80,
      render: (_, record) => (
        <Button size="small" onClick={() => openEditCodeModal(record)}>수정</Button>
      ),
    },
  ];

  return (
    <>
      <Head>
        <title>공통코드 관리 - AI 표준 도메인 추천</title>
      </Head>
      <Row gutter={16}>
        <Col span={7}>
          <Card
            title="코드 그룹"
            loading={loadingGroups}
            extra={(
              <Button size="small" icon={<PlusOutlined />} onClick={() => setGroupModalOpen(true)}>
                그룹 추가
              </Button>
            )}
          >
            <List
              dataSource={groups}
              locale={{ emptyText: "등록된 코드 그룹이 없습니다." }}
              renderItem={(g) => (
                <List.Item
                  onClick={() => handleSelectGroup(g.codeGroup)}
                  style={{
                    cursor: "pointer",
                    background: selectedGroup === g.codeGroup ? "#e6f4ff" : undefined,
                    padding: "8px 12px",
                  }}
                >
                  <List.Item.Meta title={g.groupName} description={g.codeGroup} />
                </List.Item>
              )}
            />
          </Card>
        </Col>
        <Col span={17}>
          <Card
            title={selectedGroup ? `${selectedGroup} 상세 코드` : "상세 코드"}
            extra={selectedGroup && (
              <Button size="small" type="primary" icon={<PlusOutlined />} onClick={openAddCodeModal}>
                코드 추가
              </Button>
            )}
          >
            {selectedGroup ? (
              <Table
                rowKey="codeValue"
                loading={loadingCodes}
                columns={columns}
                dataSource={codes}
                pagination={false}
              />
            ) : (
              <Empty description="왼쪽에서 코드 그룹을 먼저 선택하세요." />
            )}
          </Card>
        </Col>
      </Row>

      <Modal
        title="코드 그룹 추가"
        open={groupModalOpen}
        onCancel={() => setGroupModalOpen(false)}
        onOk={() => groupForm.submit()}
        okText="등록"
      >
        <Form form={groupForm} layout="vertical" onFinish={handleAddGroup}>
          <Form.Item
            name="codeGroup"
            label="그룹 코드"
            rules={[{ required: true, message: "그룹 코드를 입력하세요." }]}
          >
            <Input placeholder="예: DBMS_TYPE_CD" />
          </Form.Item>
          <Form.Item
            name="groupName"
            label="그룹명"
            rules={[{ required: true, message: "그룹명을 입력하세요." }]}
          >
            <Input placeholder="예: DBMS 종류" />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title={editingCode ? "코드 수정" : "코드 추가"}
        open={codeModalOpen}
        onCancel={() => setCodeModalOpen(false)}
        onOk={() => codeForm.submit()}
        okText={editingCode ? "수정" : "등록"}
      >
        <Form form={codeForm} layout="vertical" onFinish={handleSaveCode}>
          <Form.Item name="codeGroup" hidden><Input /></Form.Item>
          <Form.Item
            name="codeValue"
            label="코드값"
            rules={[{ required: true, message: "코드값을 입력하세요." }]}
          >
            <Input disabled={Boolean(editingCode)} placeholder="예: MYS" />
          </Form.Item>
          <Form.Item
            name="codeName"
            label="코드명"
            rules={[{ required: true, message: "코드명을 입력하세요." }]}
          >
            <Input placeholder="예: MySQL" />
          </Form.Item>
          <Form.Item
            name="sortOrder"
            label="정렬 순서"
            rules={[{ required: true, message: "정렬 순서를 입력하세요." }]}
          >
            <InputNumber style={{ width: "100%" }} min={0} />
          </Form.Item>
          <Form.Item name="useYn" label="사용 여부" valuePropName="checked">
            <Switch />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}

export default CommonCodesPage;
