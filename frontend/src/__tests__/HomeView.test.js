import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'

describe('HomeView', () => {
  it('contains project title', () => {
    const source = readFileSync(
      new URL('../views/HomeView.vue', import.meta.url),
      'utf-8',
    )

    expect(source).toContain('芝麻开门')
  })
})