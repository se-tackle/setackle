// API Response Types
export interface ApiResponse<T> {
  success: boolean
  data: T
  message?: string
  timestamp: string
}

export interface ApiError {
  success: false
  error: {
    code: string
    message: string
    details?: Array<{
      field: string
      message: string
    }>
  }
  timestamp: string
}

// User Types
export interface User {
  id: number
  email: string
  username: string
  role: 'USER' | 'ADMIN'
  createdAt: string
  lastLoginAt?: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface LoginResponse {
  userId: number
  email: string
  username: string
  accessToken: string
  refreshToken: string
  expiresIn: number
}

// Skill & Roadmap Types
export interface Skill {
  skillId: number
  name: string
  description: string
  category: string
  topicCount: number
  questionCount: number
}

export interface Topic {
  topicId: number
  skillId: number
  title: string
  description: string
  level: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED'
  estimatedTime: number
  questionCount: number
}

export interface RoadmapNode {
  nodeId: number
  title: string
  description: string
  level: number
  prerequisites: number[]
  resources: Resource[]
  position: {
    x: number
    y: number
  }
}

export interface Resource {
  resourceId: number
  title: string
  url: string
  type: 'DOCUMENTATION' | 'VIDEO' | 'TUTORIAL' | 'ARTICLE'
  difficulty: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED'
  estimatedTime: number
}

// Quiz Types
export interface QuizSessionRequest {
  skillId: number
  topicIds: number[]
  questionCount: number
  difficulty: 'EASY' | 'MEDIUM' | 'HARD' | 'MIXED'
}

export interface QuizSession {
  sessionId: string
  skillId: number
  questionCount: number
  timeLimit: number
  currentQuestion: Question
}

export interface Question {
  questionId: number
  questionNumber: number
  questionText: string
  options: QuestionOption[]
  timeLimit: number
}

export interface QuestionOption {
  optionId: number
  optionText: string
}

export interface AnswerRequest {
  questionId: number
  selectedOptionId: number
  timeSpent: number
}

// Report Types
export interface AssessmentReport {
  reportId: string
  sessionId: string
  skillName: string
  totalScore: number
  correctAnswers: number
  totalQuestions: number
  timeSpent: number
  completedAt: string
  topicResults: TopicResult[]
  recommendations: Recommendation[]
}

export interface TopicResult {
  topicId: number
  topicName: string
  score: number
  correctAnswers: number
  totalQuestions: number
  category: 'STRENGTH' | 'WEAKNESS'
}

export interface Recommendation {
  type: 'STUDY_TOPIC' | 'PRACTICE_MORE'
  topicId?: number
  message: string
}