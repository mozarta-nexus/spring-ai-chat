<template>
  <div class="chat-container">
    <div class="chat-header">
      <h1>Spring AI Chat</h1>
      <button @click="createNewConversation" class="new-conversation-btn" :disabled="isStreaming">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="12" y1="5" x2="12" y2="19"></line>
          <line x1="5" y1="12" x2="19" y2="12"></line>
        </svg>
        新建对话
      </button>
    </div>
    
    <div class="chat-messages" ref="messagesContainer">
      <div 
        v-for="(message, index) in messages" 
        :key="index"
        :class="['message', message.type]"
      >
        <div class="message-label">
          {{ message.type === 'user' ? '你' : 'AI' }}
        </div>
        <div class="message-content" v-html="formatMessage(message.content)"></div>
      </div>
      
      <div v-if="isStreaming" class="message assistant">
        <div class="message-label">AI</div>
        <div class="message-content streaming" v-html="formatMessage(streamingContent)"></div>
        <span class="cursor">|</span>
      </div>
    </div>
    
    <div class="chat-input-container">
      <div class="input-wrapper">
        <textarea
          v-model="userInput"
          @keydown.enter.exact.prevent="handleSend"
          @keydown.shift.enter.exact="handleNewLine"
          placeholder="输入消息... (Enter发送, Shift+Enter换行)"
          rows="4"
          class="chat-input"
          :disabled="isStreaming"
        ></textarea>
        <button 
          @click="handleSend" 
          :disabled="!userInput.trim() || isStreaming"
          class="send-button"
        >
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="22" y1="2" x2="11" y2="13"></line>
            <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
          </svg>
        </button>
      </div>
      <div class="mode-selector">
        <label>
          <input type="radio" v-model="useStream" :value="true" :disabled="isStreaming" />
          流式响应
        </label>
        <label>
          <input type="radio" v-model="useStream" :value="false" :disabled="isStreaming" />
          普通响应
        </label>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, watch } from 'vue'
import axios from 'axios'
import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/github-dark.css'

// API 基础地址
const API_BASE_URL = 'http://localhost:8080'

// 配置 marked 使用 highlight.js 进行代码高亮
marked.setOptions({
  highlight: function(code, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return hljs.highlight(code, { language: lang }).value
      } catch (err) {
        console.error('Highlight error:', err)
      }
    }
    return hljs.highlightAuto(code).value
  },
  breaks: true, // 支持 GitHub 风格的换行
  gfm: true // 启用 GitHub 风格的 Markdown
})

const messages = ref([])
const userInput = ref('')
const isStreaming = ref(false)
const streamingContent = ref('')
const useStream = ref(true)
const messagesContainer = ref(null)
const conversationId = ref(null)

// 生成 UUID
const generateUUID = () => {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    const r = Math.random() * 16 | 0
    const v = c === 'x' ? r : (r & 0x3 | 0x8)
    return v.toString(16)
  })
}

// 从 localStorage 加载对话 ID
const loadConversationId = () => {
  const saved = localStorage.getItem('conversationId')
  if (saved) {
    conversationId.value = saved
  }
}

// 保存对话 ID 到 localStorage
const saveConversationId = (id) => {
  if (id) {
    localStorage.setItem('conversationId', id)
  } else {
    localStorage.removeItem('conversationId')
  }
}

// 获取或创建对话 ID
// 如果当前没有 conversation_id，生成一个新的 UUID
const getOrCreateConversationId = () => {
  if (!conversationId.value) {
    conversationId.value = generateUUID()
    saveConversationId(conversationId.value)
  }
  return conversationId.value
}

// 创建新对话
const createNewConversation = () => {
  if (isStreaming.value) return
  conversationId.value = null
  saveConversationId(null)
  messages.value = []
  userInput.value = ''
}

// 初始化时加载对话 ID
loadConversationId()

const formatMessage = (content) => {
  if (!content) return ''
  // 使用 marked 渲染 Markdown
  try {
    return marked.parse(content)
  } catch (error) {
    console.error('Markdown parse error:', error)
    // 如果解析失败，返回原始内容（转义 HTML）
    return content.replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/\n/g, '<br>')
  }
}

const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const handleSend = async () => {
  const input = userInput.value.trim()
  if (!input || isStreaming.value) return
  
  // 添加用户消息
  messages.value.push({
    type: 'user',
    content: input
  })
  
  userInput.value = ''
  await scrollToBottom()
  
  if (useStream.value) {
    await handleStreamResponse(input)
  } else {
    await handleNormalResponse(input)
  }
}

const handleNewLine = () => {
  // Shift+Enter 换行，不做任何处理
}

const handleNormalResponse = async (input) => {
  try {
    isStreaming.value = true
    
    // 获取或创建对话 ID
    const currentConversationId = getOrCreateConversationId()
    
    const params = { 
      input,
      conversation_id: currentConversationId
    }
    
    const response = await axios.get(`${API_BASE_URL}/api/chat`, {
      params
    })
    
    messages.value.push({
      type: 'assistant',
      content: response.data
    })
    
    await scrollToBottom()
  } catch (error) {
    console.error('Error:', error)
    messages.value.push({
      type: 'assistant',
      content: '抱歉，发生了错误：' + (error.message || '未知错误')
    })
  } finally {
    isStreaming.value = false
  }
}

const handleStreamResponse = async (input) => {
  try {
    isStreaming.value = true
    streamingContent.value = ''
    
    // 获取或创建对话 ID
    const currentConversationId = getOrCreateConversationId()
    
    // 构建请求 URL，包含 conversation_id 参数
    const url = `${API_BASE_URL}/api/chat/stream?input=${encodeURIComponent(input)}&conversation_id=${encodeURIComponent(currentConversationId)}`
    
    // 使用 fetch 来处理流式响应
    const response = await fetch(url)
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')
    
    while (true) {
      const { done, value } = await reader.read()
      
      if (done) {
        break
      }
      
      // 解码数据块
      const chunk = decoder.decode(value, { stream: true })
      streamingContent.value += chunk
      
      // 实时滚动到底部
      await scrollToBottom()
    }
    
    // 流式响应完成，添加到消息列表
    messages.value.push({
      type: 'assistant',
      content: streamingContent.value
    })
    
    streamingContent.value = ''
    await scrollToBottom()
    
  } catch (error) {
    console.error('Error:', error)
    messages.value.push({
      type: 'assistant',
      content: '抱歉，发生了错误：' + (error.message || '未知错误')
    })
  } finally {
    isStreaming.value = false
  }
}

// 监听消息变化，自动滚动
watch(messages, () => {
  scrollToBottom()
}, { deep: true })
</script>

<style scoped>
.chat-container {
  background: white;
  border-radius: 20px;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.25);
  display: flex;
  flex-direction: column;
  width: 100%;
  height: calc(100vh - 40px);
  max-height: none;
  overflow: hidden;
}

.chat-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 24px 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.chat-header h1 {
  font-size: 28px;
  font-weight: 600;
  margin: 0;
  letter-spacing: -0.5px;
}

.new-conversation-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 10px;
  color: white;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  backdrop-filter: blur(10px);
}

.new-conversation-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-1px);
}

.new-conversation-btn:active:not(:disabled) {
  transform: translateY(0);
}

.new-conversation-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 32px 40px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  background: #f8f9fa;
  min-height: 0;
}

.message {
  display: flex;
  flex-direction: column;
  max-width: 75%;
  animation: fadeIn 0.3s ease-in;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message.user {
  align-self: flex-end;
}

.message.assistant {
  align-self: flex-start;
}

.message-label {
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #666;
  padding: 0 6px;
}

.message.user .message-label {
  text-align: right;
  color: #667eea;
}

.message.assistant .message-label {
  color: #764ba2;
}

.message-content {
  padding: 16px 20px;
  border-radius: 16px;
  line-height: 1.7;
  word-wrap: break-word;
  font-size: 15px;
}

/* Markdown 样式 - 通用 */
.message-content :deep(h1),
.message-content :deep(h2),
.message-content :deep(h3),
.message-content :deep(h4),
.message-content :deep(h5),
.message-content :deep(h6) {
  margin-top: 1em;
  margin-bottom: 0.5em;
  font-weight: 600;
  line-height: 1.25;
}

.message-content :deep(h1) {
  font-size: 1.5em;
}

.message-content :deep(h2) {
  font-size: 1.3em;
}

.message-content :deep(h3) {
  font-size: 1.1em;
}

.message-content :deep(p) {
  margin: 0.5em 0;
}

.message-content :deep(ul),
.message-content :deep(ol) {
  margin: 0.5em 0;
  padding-left: 2em;
}

.message-content :deep(li) {
  margin: 0.25em 0;
}

.message-content :deep(blockquote) {
  margin: 0.5em 0;
  padding: 0 1em;
  border-left: 4px solid #ddd;
  color: #666;
}

.message-content :deep(code) {
  padding: 0.2em 0.4em;
  margin: 0;
  font-size: 85%;
  background-color: rgba(0, 0, 0, 0.05);
  border-radius: 3px;
  font-family: 'Courier New', Courier, monospace;
}

.message-content :deep(pre) {
  padding: 1em;
  margin: 0.5em 0;
  overflow: auto;
  background-color: #f6f8fa;
  border-radius: 6px;
  line-height: 1.45;
}

.message-content :deep(pre code) {
  display: block;
  padding: 0;
  margin: 0;
  overflow: visible;
  word-wrap: normal;
  background-color: transparent;
  border: 0;
  font-size: 100%;
}

.message-content :deep(table) {
  border-collapse: collapse;
  margin: 0.5em 0;
  width: 100%;
}

.message-content :deep(th),
.message-content :deep(td) {
  border: 1px solid #ddd;
  padding: 0.5em;
  text-align: left;
}

.message-content :deep(th) {
  font-weight: 600;
  background-color: #f6f8fa;
}

.message-content :deep(a) {
  color: #667eea;
  text-decoration: underline;
}

.message-content :deep(a:hover) {
  color: #764ba2;
}

.message-content :deep(hr) {
  height: 0.25em;
  padding: 0;
  margin: 1.5em 0;
  background-color: #e0e0e0;
  border: 0;
}

.message-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 4px;
  margin: 0.5em 0;
}

/* AI 消息中的 Markdown 样式（白色背景） */
.message.assistant .message-content :deep(h1),
.message.assistant .message-content :deep(h2) {
  border-bottom: 1px solid #e0e0e0;
  padding-bottom: 0.3em;
}

/* 用户消息中的 Markdown 样式（深色背景） */
.message.user .message-content :deep(blockquote) {
  border-left-color: rgba(255, 255, 255, 0.4);
  color: rgba(255, 255, 255, 0.9);
}

.message.user .message-content :deep(code) {
  background-color: rgba(0, 0, 0, 0.2);
  color: #fff;
}

.message.user .message-content :deep(pre) {
  background-color: rgba(0, 0, 0, 0.3);
}

.message.user .message-content :deep(pre code) {
  color: #fff;
}

.message.user .message-content :deep(h1),
.message.user .message-content :deep(h2) {
  border-bottom: 1px solid rgba(255, 255, 255, 0.3);
  padding-bottom: 0.3em;
}

.message.user .message-content :deep(th),
.message.user .message-content :deep(td) {
  border-color: rgba(255, 255, 255, 0.3);
}

.message.user .message-content :deep(th) {
  background-color: rgba(255, 255, 255, 0.15);
}

.message.user .message-content :deep(hr) {
  background-color: rgba(255, 255, 255, 0.3);
}

.message.user .message-content :deep(a) {
  color: #90caf9;
}

.message.user .message-content :deep(a:hover) {
  color: #64b5f6;
}

/* 用户消息中的 Markdown 样式（白色背景） */
.message.user .message-content :deep(blockquote) {
  border-left-color: rgba(102, 126, 234, 0.5);
  color: rgba(255, 255, 255, 0.95);
}

.message.user .message-content :deep(code) {
  background-color: rgba(0, 0, 0, 0.15);
}

.message.user .message-content :deep(pre) {
  background-color: rgba(0, 0, 0, 0.2);
}

.message.user .message-content :deep(h1),
.message.user .message-content :deep(h2) {
  border-bottom-color: rgba(255, 255, 255, 0.3);
}

.message.user .message-content :deep(th),
.message.user .message-content :deep(td) {
  border-color: rgba(255, 255, 255, 0.3);
}

.message.user .message-content :deep(th) {
  background-color: rgba(255, 255, 255, 0.15);
}

.message.user .message-content :deep(hr) {
  background-color: rgba(255, 255, 255, 0.3);
}

.message.user .message-content {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-bottom-right-radius: 4px;
}

.message.assistant .message-content {
  background: white;
  color: #333;
  border: 1px solid #e0e0e0;
  border-bottom-left-radius: 4px;
}

/* Markdown 样式 - 通用 */
.message-content :deep(h1),
.message-content :deep(h2),
.message-content :deep(h3),
.message-content :deep(h4),
.message-content :deep(h5),
.message-content :deep(h6) {
  margin-top: 1em;
  margin-bottom: 0.5em;
  font-weight: 600;
  line-height: 1.25;
}

.message-content :deep(h1) {
  font-size: 1.5em;
}

.message-content :deep(h2) {
  font-size: 1.3em;
}

.message-content :deep(h3) {
  font-size: 1.1em;
}

.message-content :deep(p) {
  margin: 0.5em 0;
}

.message-content :deep(ul),
.message-content :deep(ol) {
  margin: 0.5em 0;
  padding-left: 2em;
}

.message-content :deep(li) {
  margin: 0.25em 0;
}

.message-content :deep(blockquote) {
  margin: 0.5em 0;
  padding: 0 1em;
  border-left: 4px solid #ddd;
  color: #666;
}

.message-content :deep(code) {
  padding: 0.2em 0.4em;
  margin: 0;
  font-size: 85%;
  background-color: rgba(0, 0, 0, 0.05);
  border-radius: 3px;
  font-family: 'Courier New', Courier, monospace;
}

.message-content :deep(pre) {
  padding: 1em;
  margin: 0.5em 0;
  overflow: auto;
  background-color: #f6f8fa;
  border-radius: 6px;
  line-height: 1.45;
}

.message-content :deep(pre code) {
  display: block;
  padding: 0;
  margin: 0;
  overflow: visible;
  word-wrap: normal;
  background-color: transparent;
  border: 0;
  font-size: 100%;
}

.message-content :deep(table) {
  border-collapse: collapse;
  margin: 0.5em 0;
  width: 100%;
}

.message-content :deep(th),
.message-content :deep(td) {
  border: 1px solid #ddd;
  padding: 0.5em;
  text-align: left;
}

.message-content :deep(th) {
  font-weight: 600;
  background-color: #f6f8fa;
}

.message-content :deep(a) {
  color: #667eea;
  text-decoration: underline;
}

.message-content :deep(a:hover) {
  color: #764ba2;
}

.message-content :deep(hr) {
  height: 0.25em;
  padding: 0;
  margin: 1.5em 0;
  background-color: #e0e0e0;
  border: 0;
}

.message-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 4px;
  margin: 0.5em 0;
}

/* 用户消息中的 Markdown 样式（深色背景） */
.message.user .message-content :deep(blockquote) {
  border-left-color: rgba(255, 255, 255, 0.4);
  color: rgba(255, 255, 255, 0.9);
}

.message.user .message-content :deep(code) {
  background-color: rgba(0, 0, 0, 0.2);
  color: #fff;
}

.message.user .message-content :deep(pre) {
  background-color: rgba(0, 0, 0, 0.3);
}

.message.user .message-content :deep(pre code) {
  color: #fff;
}

.message.user .message-content :deep(h1),
.message.user .message-content :deep(h2) {
  border-bottom: 1px solid rgba(255, 255, 255, 0.3);
  padding-bottom: 0.3em;
}

.message.user .message-content :deep(th),
.message.user .message-content :deep(td) {
  border-color: rgba(255, 255, 255, 0.3);
}

.message.user .message-content :deep(th) {
  background-color: rgba(255, 255, 255, 0.15);
}

.message.user .message-content :deep(hr) {
  background-color: rgba(255, 255, 255, 0.3);
}

.message.user .message-content :deep(a) {
  color: #90caf9;
}

.message.user .message-content :deep(a:hover) {
  color: #64b5f6;
}

.message-content.streaming {
  position: relative;
}

.cursor {
  display: inline-block;
  animation: blink 1s infinite;
  color: #667eea;
  font-weight: bold;
  margin-left: 2px;
}

@keyframes blink {
  0%, 50% {
    opacity: 1;
  }
  51%, 100% {
    opacity: 0;
  }
}

.chat-input-container {
  border-top: 1px solid #e0e0e0;
  padding: 16px;
  background: white;
}

.input-wrapper {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.chat-input {
  flex: 1;
  padding: 14px 20px;
  border: 2px solid #e0e0e0;
  border-radius: 14px;
  font-size: 15px;
  font-family: inherit;
  resize: none;
  outline: none;
  transition: border-color 0.2s;
  line-height: 1.5;
}

.chat-input:focus {
  border-color: #667eea;
}

.chat-input:disabled {
  background: #f5f5f5;
  cursor: not-allowed;
}

.send-button {
  width: 52px;
  height: 52px;
  border: none;
  border-radius: 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s, opacity 0.2s, box-shadow 0.2s;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.send-button:hover:not(:disabled) {
  transform: scale(1.05);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

.send-button:active:not(:disabled) {
  transform: scale(0.95);
}

.send-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.mode-selector {
  display: flex;
  gap: 20px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.mode-selector label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  color: #666;
  cursor: pointer;
  font-weight: 500;
}

.mode-selector input[type="radio"] {
  cursor: pointer;
}

.mode-selector input[type="radio"]:disabled {
  cursor: not-allowed;
}

/* 滚动条样式 */
.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: #555;
}
</style>

