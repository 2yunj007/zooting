import { localAxios } from "@/util/http-commons";

const local = localAxios();

async function checkNicknameApi(param: string, success: any, fail: any) {
  local.defaults.headers["Authorization"] = `Bearer ${await localStorage.getItem("accessToken")}`;
  await local.get(`/api/members/nickname/check?nickname=${param}`).then(success).catch(fail);
}

async function updateNicknameApi(body: any, success: any, fail: any) {
  local.defaults.headers["Authorization"] = `Bearer ${await localStorage.getItem("accessToken")}`;
  await local.put(`/api/members/nickname`, body).then(success).catch(fail);
}

async function loadMyInfoApi(success: any, fail: any) {
  local.defaults.headers["Authorization"] = `Bearer ${await localStorage.getItem("accessToken")}`;
  await local.get(`/api/members`).then(success).catch(fail);
}

// body로는 json 데이터
// success, fail에는 각각 성공시 반환되는 json 데이터와 실패시 생기는 error 리턴
async function updateMyInfoApi(body: any, success: any, fail: any) {
  local.defaults.headers["Authorization"] = `Bearer ${await localStorage.getItem("accessToken")}`;
  await local.patch(`api/members/info`, body).then(success).catch(fail);
}

async function loadUserInfoApi(param: string, success: any, fail: any) {
  local.defaults.headers["Authorization"] = `Bearer ${await localStorage.getItem("accessToken")}`;
  await local.get(`api/members/info?nickname=${param}`).then(success).catch(fail);
}

async function checkIsMyProfileApi(param: string, success: any, fail: any) {
  local.defaults.headers["Authorization"] = `Bearer ${await localStorage.getItem("accessToken")}`;
  await local.get(`api/members/myprofile/check?nickname=${param}`).then(success).catch(fail);
}

async function updateIntroduceApi(body: any, success: any, fail: any) {
  local.defaults.headers["Authorization"] = `Bearer ${await localStorage.getItem("accessToken")}`;
  await local.put(`/api/members/introduce`, body).then(success).catch(fail);
}
export {
  loadMyInfoApi,
  updateMyInfoApi,
  loadUserInfoApi,
  checkIsMyProfileApi,
  updateIntroduceApi,
  checkNicknameApi,
  updateNicknameApi,
};
