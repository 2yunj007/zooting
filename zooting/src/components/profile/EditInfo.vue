<script setup lang="ts">
import {computed, onMounted, ref, watch} from "vue";
import {useRouter} from "vue-router";
import VueTailwindDatepicker from "vue-tailwind-datepicker";
import SuccessDialog from "@/components/profile/SuccessDialog.vue";
import FailDialog from "@/components/profile/FailDialog.vue";
import {RadioGroup, RadioGroupLabel, RadioGroupOption} from "@headlessui/vue";
import {useAccessTokenStore} from "@/stores/store";
import {checkNicknameApi, updateMyInfoApi, updateNicknameApi,} from "@/api/profile";

const store = useAccessTokenStore();
const router = useRouter();
const emits = defineEmits([
  "loadMyInfo",
]);
const props = defineProps({
  isMyProfile: Boolean,
});

const address = ref<string | null>();
const nickname = ref<string | null>();
const isNicknameUpdatable = ref<boolean>(false);
const isNicknameVerify = ref<boolean>(false);
const gender = ref<string>("");
const idealTypeSet = ref(new Set<string>());
const birth = ref<string | null>(null);
const failAlert = ref(false);
const failMessage = ref<string>();
const successAlert = ref(false);

const myInfo = ref(store.userInfo);

const toggleNicknameUpdateStatus = () => {
  isNicknameUpdatable.value = !isNicknameUpdatable.value;
};

// 나의 정보 수정
const updateMyInfo = async () => {
  // 유효성 검증
  if (!myInfo.value!.address) {
    failMessage.value = "정확한 지역을 입력해 주세요";
    failAlert.value = true;
    return;
  }
  if (idealTypeSet.value.size == 0) {
    failMessage.value = "이상형 동물상을 선택해 주세요";
    failAlert.value = true;
    return;
  }

  const data = {
    address: address.value,
    idealAnimal: Array.from(idealTypeSet.value),
  };

  await updateMyInfoApi(
      data,
      async () => {
        successAlert.value = true;
        await store.getUserInfo();
        updateChanges();

        // sidebar가 내 정보 인 경우 수정
        if (props.isMyProfile) {
          emits("loadMyInfo");
        }
      },
      (error: any) => console.log(error)
  );
};

const executeUpdateNickname = () => {
  if (!nickname.value) return;

  const data = {
    nickname: nickname.value,
  };

  updateNicknameApi(
      data,
      async ({data}: any) => {
        // 유저 정보 업데이트
        await store.getUserInfo();
        updateChanges();
        // sidebar가 내 정보 인 경우 수정
        if (props.isMyProfile) {
          emits("loadMyInfo");
        }

        // 마이페이지로 리다이렉트
        await router.replace({
          name: "home",
          force: true,
        });

        isNicknameUpdatable.value = false;
        isNicknameVerify.value = false;
      },
      async (error: any) => {
        await store.getUserInfo();
        updateChanges();
        isNicknameUpdatable.value = false;
        isNicknameVerify.value = false;
      }
  );
};

const cancelUpdateNickname = () => {
  if (myInfo.value) {
    nickname.value = myInfo.value.nickname;
  }
  isNicknameUpdatable.value = false;
  isNicknameVerify.value = false;
};

const checkNickname = async (name: string) => {
  if (!name) {
    return;
  }
  await checkNicknameApi(
      name,
      ({data}: any) => {
        if (data["result"] === true) {
          isNicknameVerify.value = false; // 닉네임 중복
        } else {
          isNicknameVerify.value = true; // 닉네임 중복 X
        }
      },
      (error: any) => console.log(error)
  );
};

const setFailAlert = (isOpen: boolean) => {
  failAlert.value = isOpen;
};
const setSuccessAlert = (isOpen: boolean) => {
  successAlert.value = isOpen;
};

const updateChanges = () => {
  if (!myInfo.value) return;

  if (myInfo.value.gender) {
    gender.value = myInfo.value.gender;
  }
  if (myInfo.value.birth) {
    birth.value = convertDate(myInfo.value.birth);
  }
  nickname.value = myInfo.value.nickname;
  address.value = myInfo.value.address;
  idealTypeSet.value = parseStringToSet(myInfo.value!.idealAnimal);
};

const areas: string[] = [
  "서울",
  "부산",
  "대구",
  "인천",
  "광주",
  "대전",
  "울산",
  "세종",
  "경기도",
  "강원도",
  "충청북도",
  "충청남도",
  "전라북도",
  "전라남도",
  "경상북도",
  "경상남도",
  "제주도",
  "해외",
];

const getGenderLabel = (value: string) => {
  return value === "man" ? "남자" : "여자";
};

const formatter = ref<{ date: string; month: string }>({
  date: "YYYY-MM-DD",
  month: "MMM",
});
const parseStringToSet = (stringList: string) => {
  try {
    // 대괄호를 제거하고 쉼표로 구분하여 배열로 변환
    const parsedList = stringList
        .replace(/^\[|\]$/g, "") // 대괄호 제거
        .split(", ")
        .map((item) => item.trim()); // 양쪽 공백 제거

    // 만약 배열이 아니면 예외 발생
    if (!Array.isArray(parsedList)) {
      throw new Error("Invalid input: Not an array.");
    }

    return new Set<string>(parsedList);
  } catch (error) {
    return new Set<string>(); // 파싱에 실패하면 null 반환
  }
};

const convertDate = (inputDate: string | null) => {
  if (!inputDate) {
    return "1900-01-01";
  }
  // 주어진 문자열을 Date 객체로 변환
  const originalDate = new Date(inputDate);

  // 날짜를 하루 더함
  originalDate.setDate(originalDate.getDate() + 1);

  // 날짜를 원하는 형식으로 포맷
  return originalDate.toISOString().split("T")[0];
};

const moveToMyPage = () => {
  if (!myInfo.value) return;

  router.push({
    name: "profile-check",
    params: {nickname: myInfo.value.nickname},
  });
};

const updateNicknameValue = (event: any) => {
  nickname.value = event.target.value;
};

watch(() => store.userInfo, (updateUser) => {
  myInfo.value = updateUser;
})

watch(nickname, async (newValue, oldValue) => {
  if (!newValue) return;

  await checkNickname(newValue);
});

onMounted(async () => {
  await store.getUserInfo();
  updateChanges();
});
</script>

<template>
  <div>
    <FailDialog
        title="업데이트 실패!"
        :message="failMessage"
        :fail-alert="failAlert"
        @set-fail-alert="setFailAlert"
    />
    <SuccessDialog
        title="업데이트 성공!"
        message="회원정보 업데이트 완료!"
        :success-alert="successAlert"
        @set-success-alert="setSuccessAlert"
        @on-click-submit="moveToMyPage"
    />
    <div class="flex flex-col gap-10 p-5">
      <div class="flex flex-row">
        <div @click="moveToMyPage()" class="flex flex-col items-center ml-4">
          <svg
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              viewBox="0 0 24 24"
              stroke-width="1.5"
              stroke="currentColor"
              class="w-10 h-10 mx-auto stroke-orange-500 fill-rose-100 hover:fill-rose-300"
          >
            <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M8.25 9V5.25A2.25 2.25 0 0 1 10.5 3h6a2.25 2.25 0 0 1 2.25 2.25v13.5A2.25 2.25 0 0 1 16.5 21h-6a2.25 2.25 0 0 1-2.25-2.25V15m-3 0-3-3m0 0 3-3m-3 3H15"
            />
          </svg>
          <p class="font-sans text-xs font-semibold tracking-tight text-center">마이페이지</p>
        </div>
      </div>
      <div class="flex flex-col items-center justify-center w-full gap-5">
        <div class="flex flex-col items-center w-3/4 gap-5">
          <div class="relative input__div">
            <label
                for="nickname"
                class="absolute inline-block bg-transparent input__label -top-7 left-10"
            >닉네임</label
            >
            <input
                type="text"
                name="nickname"
                id="nickname"
                :class="isNicknameUpdatable ? 'input__nickname_enabled' : 'input__nickname_disabled'"
                :value="nickname"
                @input="updateNicknameValue"
                :disabled="!isNicknameUpdatable"
            />
            <div v-if="!isNicknameUpdatable">
              <svg
                  xmlns="http://www.w3.org/2000/svg"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke-width="1.3"
                  stroke="currentColor"
                  class="cursor-pointer btn__nickname_enabled"
                  @click="toggleNicknameUpdateStatus"
              >
                <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    d="m16.862 4.487 1.687-1.688a1.875 1.875 0 1 1 2.652 2.652L10.582 16.07a4.5 4.5 0 0 1-1.897 1.13L6 18l.8-2.685a4.5 4.5 0 0 1 1.13-1.897l8.932-8.931Zm0 0L19.5 7.125M18 14v4.75A2.25 2.25 0 0 1 15.75 21H5.25A2.25 2.25 0 0 1 3 18.75V8.25A2.25 2.25 0 0 1 5.25 6H10"
                />
              </svg>
            </div>
            <div v-if="isNicknameUpdatable">
              <svg
                  v-if="isNicknameVerify"
                  xmlns="http://www.w3.org/2000/svg"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke-width="1.5"
                  stroke="currentColor"
                  class="cursor-pointer btn__nickname_disabled"
                  @click="executeUpdateNickname()"
              >
                <path stroke-linecap="round" stroke-linejoin="round" d="m4.5 12.75 6 6 9-13.5"/>
              </svg>
              <p v-if="isNicknameVerify" class="absolute text-xs font-semibold text-blue-600 -bottom-4 right-10">
                사용 가능한 닉네임 (50 Point 소요)
              </p>
              <svg
                  v-if="!isNicknameVerify"
                  xmlns="http://www.w3.org/2000/svg"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke-width="1.5"
                  stroke="currentColor"
                  class="cursor-pointer btn__nickname_disabled"
                  @click="cancelUpdateNickname()"
              >
                <path stroke-linecap="round" stroke-linejoin="round" d="M6 18 18 6M6 6l12 12"/>
              </svg>
  
              <p v-if="!isNicknameVerify" class="absolute text-xs font-semibold text-red-600 -bottom-4 right-10">
                닉네임이 중복 됩니다.
              </p>
            </div>
          </div>
          <div class="input__div">
            <label for="gender" class="input__label">성별</label>
            <RadioGroup v-model="gender" :disabled="true">
              <div class="gender">
                <RadioGroupOption
                    as="template"
                    v-for="gender in ['man', 'woman']"
                    :key="gender"
                    :value="gender"
                    v-slot="{ checked }"
                >
                  <div
                      :class="[
                      checked ? 'gender__option--checked' : 'gender__option--no-checked',
                      'gender__option',
                    ]"
                  >
                    <RadioGroupLabel as="span">{{ getGenderLabel(gender) }}</RadioGroupLabel>
                  </div>
                </RadioGroupOption>
              </div>
            </RadioGroup>
          </div>
          <div class="input__div">
            <label for="birth" class="input__label">생년월일</label>
            <VueTailwindDatepicker
                id="birth"
                v-model="birth"
                as-single
                :formatter="formatter"
                weekdays-size="min"
                class="text-lg font-bold text-center cursor-pointer hover:bg-gray-200"
                :disabled="true"
            />
          </div>
          <div class="input__div">
            <label for="address" class="input__label">지역</label>
            <select id="address" v-model="address" class="cursor-pointer">
              <option value="" disabled selected hidden>사는 지역을 선택해 주세요.</option>
              <option v-for="(area, index) in areas" :key="index">{{ area }}</option>
            </select>
          </div>
        </div>
        <div class="flex flex-row justify-end w-3/4 gap-3 px-20 m-10">
          <button type="button" @click="updateChanges" class="btn__save">초기화</button>
          <button type="button" @click="updateMyInfo" class="btn__cancel">저장</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.input__nickname_enabled {
  @apply block w-full px-8 py-2 text-lg font-bold text-center text-gray-900 border-0 rounded-md shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:leading-6;
}

.input__nickname_disabled {
  @apply block w-full px-8 py-2 text-lg font-bold text-center text-gray-900 bg-gray-100 border-0 rounded-md shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:leading-6;
}

.btn__nickname_enabled {
  @apply w-7 h-7 absolute bottom-1 right-12 hover:stroke-indigo-600;
}

.btn__nickname_disabled {
  @apply w-7 h-7 absolute bottom-1 right-12 fill-orange-300 hover:stroke-orange-700;
}

.input__div {
  @apply w-5/6 px-10;
}

.input__label {
  @apply font-semibold text-gray-900;
}

.input__div select {
  @apply border border-gray-300 text-gray-900 text-lg font-semibold rounded-lg focus:ring-violet-500 focus:border-violet-500 block w-full px-4 py-3;
}

.gender {
  @apply grid grid-cols-2 gap-3;
}

.gender__option {
  @apply flex items-center justify-center rounded-lg p-2 text-sm uppercase md:flex-1 bg-gray-200;
}

.gender__option--checked {
  @apply bg-violet-600 text-white font-bold text-lg;
}

.gender__option--no-checked {
  @apply ring-1 ring-inset ring-gray-300 text-lg font-bold text-gray-900 hover:bg-gray-200;
}

.ideal-type__div {
  @apply mb-6 grid grid-cols-5 gap-3;
  height: 80px;
}

.ideal-type__item {
  @apply border rounded-md text-center flex justify-center items-center;
}

.ideal-type__item--checked {
  @apply bg-violet-600 hover:bg-violet-500;
}

.ideal-type__item--no-checked {
  @apply border-gray-300 hover:bg-gray-50;
}

.btn__save {
  @apply w-32 py-2 px-4 text-xl font-semibold text-white bg-orange-400 rounded-lg shadow-sm hover:bg-orange-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-orange-300;
}

.btn__cancel {
  @apply w-32 py-2 px-4 text-xl font-semibold text-white bg-indigo-600 rounded-lg shadow-sm hover:bg-indigo-700 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-500;
}
</style>
