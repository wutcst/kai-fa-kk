import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import HomeView from '../views/HomeView.vue'

describe('HomeView', () => {
  it('renders project title', () => {
    const wrapper = mount(HomeView)
    expect(wrapper.text()).toContain('芝麻开门')
  })
})