<script setup>
import { ref } from 'vue'
import sesameLogo from '../assets/images/logo/sesame-logo.png'

defineProps({
  intro: {
    type: Object,
    required: true,
  },
})

defineEmits(['enter-game'])

const viewMode = ref('landing')
const authMode = ref('login')

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
}
</script>

<template>
  <section
    class="auth-view"
    :class="`auth-view--${viewMode}`"
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
          @submit.prevent="$emit('enter-game')"
        >
          <label>
            <span>用户名</span>
            <input
              placeholder="请输入用户名"
              autocomplete="username"
            >
          </label>
          <label>
            <span>密码</span>
            <input
              placeholder="请输入密码"
              type="password"
              autocomplete="current-password"
            >
          </label>
          <button type="submit">
            进入秘窟
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
          @submit.prevent
        >
          <label>
            <span>用户名</span>
            <input
              placeholder="设置登录用户名"
              autocomplete="username"
            >
          </label>
          <label>
            <span>昵称</span>
            <input placeholder="设置寻宝者昵称">
          </label>
          <label>
            <span>密码</span>
            <input
              placeholder="设置密码"
              type="password"
              autocomplete="new-password"
            >
          </label>
          <button type="button">
            创建寻宝者
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
