<script setup>
import { computed, ref } from 'vue'
import sesameLogo from '../assets/images/logo/sesame-logo.png'
import { getPageBackground } from '../utils/assetMap'

const props = defineProps({
  intro: {
    type: Object,
    required: true,
  },
  loading: {
    type: Boolean,
    default: false,
  },
  errorMessage: {
    type: String,
    default: '',
  },
})

const emit = defineEmits(['login', 'register'])

const viewMode = ref('landing')
const authMode = ref('login')
const localErrorMessage = ref('')
const loginForm = ref({
  username: '',
  password: '',
})
const registerForm = ref({
  username: '',
  password: '',
})
const displayedErrorMessage = computed(() => localErrorMessage.value || props.errorMessage)
const authBackground = computed(() => {
  if (viewMode.value === 'intro') return getPageBackground('intro')
  if (viewMode.value === 'auth') return getPageBackground('auth')
  return getPageBackground('home')
})

const openAuth = (mode = 'login') => {
  authMode.value = mode
  viewMode.value = 'auth'
}

const showIntro = () => {
  viewMode.value = 'intro'
}

const backToLanding = () => {
  viewMode.value = 'landing'
  authMode.value = 'login'
  localErrorMessage.value = ''
}

const validateCredentials = ({ username, password }) => {
  if (!username.trim() || !password) {
    localErrorMessage.value = '用户名和密码不能为空'
    return false
  }

  localErrorMessage.value = ''
  return true
}

const submitLogin = () => {
  if (!validateCredentials(loginForm.value)) return

  emit('login', {
    username: loginForm.value.username.trim(),
    password: loginForm.value.password,
  })
}

const submitRegister = () => {
  if (!validateCredentials(registerForm.value)) return

  emit('register', {
    username: registerForm.value.username.trim(),
    password: registerForm.value.password,
  })
}
</script>

<template>
  <section
    class="auth-view"
    :class="`auth-view--${viewMode}`"
    :style="{ '--auth-background': `url(${authBackground})` }"
    aria-label="芝麻开门入口页"
  >
    <section
      v-if="viewMode === 'landing'"
      class="auth-landing"
      aria-labelledby="landing-title"
    >
      <img
        class="auth-logo-image"
        :src="sesameLogo"
        alt="芝麻开门游戏 Logo"
      >
      <p class="auth-subtitle">
        {{ intro.subtitle }}
      </p>
      <div class="landing-actions">
        <button
          type="button"
          class="landing-button landing-button--primary"
          @click="openAuth('login')"
        >
          登录 / 注册
        </button>
        <button
          type="button"
          class="landing-button landing-button--secondary"
          @click="showIntro"
        >
          游戏介绍
        </button>
      </div>
    </section>

    <section
      v-else-if="viewMode === 'intro'"
      class="auth-landing auth-landing--intro"
      aria-labelledby="intro-title"
    >
      <div class="intro-panel">
        <div class="intro-panel__header">
          <span>游戏介绍</span>
          <h1 id="intro-title">
            {{ intro.briefingTitle }}
          </h1>
          <p>{{ intro.briefingSubtitle }}</p>
        </div>
        <div class="intro-briefing">
          <article class="intro-scroll-block intro-scroll-block--lore">
            <div class="intro-scroll-block__heading">
              <span class="intro-scroll-block__index">{{ intro.sections[0].id }}</span>
              <h3>{{ intro.sections[0].title }}</h3>
            </div>
            <p
              v-for="paragraph in intro.sections[0].paragraphs"
              :key="paragraph"
            >
              {{ paragraph }}
            </p>
            <blockquote class="intro-quote">
              <span>古卷摘录</span>
              {{ intro.quote }}
            </blockquote>
          </article>

          <div class="intro-briefing__side">
            <article class="intro-scroll-block">
              <div class="intro-scroll-block__heading">
                <span class="intro-scroll-block__index">{{ intro.sections[1].id }}</span>
                <h3>{{ intro.sections[1].title }}</h3>
              </div>
              <p>
                {{ intro.sections[1].content }}
              </p>
            </article>

            <article class="intro-scroll-block">
              <div class="intro-scroll-block__heading">
                <span class="intro-scroll-block__index">{{ intro.sections[2].id }}</span>
                <h3>{{ intro.sections[2].title }}</h3>
              </div>
              <ul>
                <li
                  v-for="point in intro.sections[2].points"
                  :key="point"
                >
                  {{ point }}
                </li>
              </ul>
            </article>

            <article class="intro-scroll-block intro-scroll-block--victory">
              <div class="intro-scroll-block__heading">
                <span class="intro-scroll-block__index">04</span>
                <h3>{{ intro.victory.title }}</h3>
              </div>
              <p>
                {{ intro.victory.content }}
              </p>
            </article>
          </div>
        </div>
        <div class="intro-actions">
          <button
            type="button"
            class="landing-button landing-button--primary"
            @click="openAuth('login')"
          >
            登录 / 注册
          </button>
          <button
            type="button"
            class="landing-button landing-button--secondary"
            @click="backToLanding"
          >
            返回入口
          </button>
        </div>
      </div>
    </section>

    <section
      v-else
      class="auth-window"
      aria-labelledby="auth-panel-title"
    >
      <div class="auth-art">
        <div
          class="portal-glow"
          aria-hidden="true"
        />
        <div class="auth-art-copy">
          <span>探索秘窟</span>
          <h2>暗语藏在石门之后</h2>
          <p>辨认线索，推开石门，向蓝色传送门深处前进。</p>
        </div>
      </div>

      <div class="auth-panel">
        <button
          type="button"
          class="auth-close"
          aria-label="关闭认证窗口"
          @click="backToLanding"
        >
          关闭
        </button>

        <span class="auth-panel__eyebrow">
          身份确认
        </span>
        <h2 id="auth-panel-title">
          {{ authMode === 'login' ? '登录游戏' : '注册账号' }}
        </h2>
        <p class="auth-panel__lead">
          {{ authMode === 'login' ? '输入暗语凭证，踏入石门后的秘窟。' : '创建寻宝者身份，准备进入第一次探索。' }}
        </p>

        <p
          v-if="displayedErrorMessage"
          class="auth-form-error"
          role="alert"
        >
          {{ displayedErrorMessage }}
        </p>

        <div
          class="auth-tabs"
          role="tablist"
          aria-label="登录或注册"
        >
          <button
            :class="{ active: authMode === 'login' }"
            type="button"
            @click="authMode = 'login'"
          >
            登录
          </button>
          <button
            :class="{ active: authMode === 'register' }"
            type="button"
            @click="authMode = 'register'"
          >
            注册
          </button>
        </div>

        <form
          v-if="authMode === 'login'"
          class="auth-form"
          @submit.prevent="submitLogin"
        >
          <label>
            <span>用户名</span>
            <input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              autocomplete="username"
            >
          </label>
          <label>
            <span>密码</span>
            <input
              v-model="loginForm.password"
              placeholder="请输入密码"
              type="password"
              autocomplete="current-password"
            >
          </label>
          <button
            type="submit"
            :disabled="loading"
          >
            {{ loading ? '请求中...' : '进入秘窟' }}
          </button>
          <button
            type="button"
            class="auth-switch"
            @click="authMode = 'register'"
          >
            注册新账号
          </button>
        </form>

        <form
          v-else
          class="auth-form"
          @submit.prevent="submitRegister"
        >
          <label>
            <span>用户名</span>
            <input
              v-model="registerForm.username"
              placeholder="设置登录用户名"
              autocomplete="username"
            >
          </label>
          <label>
            <span>密码</span>
            <input
              v-model="registerForm.password"
              placeholder="设置密码"
              type="password"
              autocomplete="new-password"
            >
          </label>
          <button
            type="submit"
            :disabled="loading"
          >
            {{ loading ? '请求中...' : '创建寻宝者' }}
          </button>
          <button
            type="button"
            class="auth-switch"
            @click="authMode = 'login'"
          >
            已有账号，去登录
          </button>
        </form>
      </div>
    </section>
  </section>
</template>

<style scoped>
.auth-form-error {
  margin: 14px 0 0;
  padding: 10px 12px;
  border: 1px solid rgba(145, 60, 42, 0.42);
  border-radius: 8px;
  color: #7d2d1f;
  background: rgba(255, 235, 224, 0.84);
  font-size: 14px;
  font-weight: 700;
  line-height: 1.45;
}

.auth-form button:disabled {
  cursor: wait;
  opacity: 0.62;
  transform: none;
}
</style>
