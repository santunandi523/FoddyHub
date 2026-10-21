import React, { useState, useEffect, useRef } from 'react';
import './AiChatWidget.css';
import { sendChatMessage, clearProposedOrder, getProposedOrder } from '../../Service/aiService';
import FoodRecommendationCard from './FoodRecommendationCard';
import ProposedOrderCard from './ProposedOrderCard';
import { toast } from 'react-toastify';

const QUICK_PROMPTS = [
  "Spicy meal for two under ₹500",
  "Find vegetarian food under ₹200",
  "Suggest a high-protein dinner",
  "Show me the cheapest pizza",
  "Build me a meal under 600 calories with at least 30g protein",
  "Show my proposed order"
];

const AiChatWidget = ({ isFullPage = false }) => {
  const [isOpen, setIsOpen] = useState(isFullPage);
  const [isMinimized, setIsMinimized] = useState(false);
  const [messages, setMessages] = useState([
    {
      id: 'welcome',
      role: 'assistant',
      text: "👋 Hi! I'm Foodie AI — Your Personal Food Assistant. Tell me what you're craving, your budget, or your nutrition goals, and I'll find or build the perfect meal for you!",
      suggestedActions: ["Spicy meal for two under ₹500", "High protein meal", "Vegetarian under ₹200"]
    }
  ]);
  const [input, setInput] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [conversationId, setConversationId] = useState(null);
  const [activeProposedOrder, setActiveProposedOrder] = useState(null);
  const [lastFailedMessage, setLastFailedMessage] = useState(null);

  const messagesEndRef = useRef(null);
  const inputRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    if (isOpen && !isMinimized) {
      scrollToBottom();
      inputRef.current?.focus();
    }
  }, [messages, isOpen, isMinimized]);

  // Sync proposed order on open
  useEffect(() => {
    if (isOpen) {
      getProposedOrder()
        .then((order) => {
          if (order && order.items && order.items.length > 0) {
            setActiveProposedOrder(order);
          }
        })
        .catch(() => {});
    }
  }, [isOpen]);

  const handleSendMessage = async (textToSend) => {
    const messageText = textToSend || input;
    if (!messageText.trim() || isLoading) return;

    const userMsg = {
      id: 'usr_' + Date.now(),
      role: 'user',
      text: messageText.trim()
    };

    setMessages((prev) => [...prev, userMsg]);
    setInput("");
    setIsLoading(true);
    setLastFailedMessage(null);

    try {
      const response = await sendChatMessage(messageText.trim(), conversationId);
      if (response.conversationId) {
        setConversationId(response.conversationId);
      }

      if (response.proposedOrder) {
        setActiveProposedOrder(response.proposedOrder);
      }

      const assistantMsg = {
        id: 'ast_' + Date.now(),
        role: 'assistant',
        text: response.message,
        foodRecommendations: response.foodRecommendations || [],
        proposedOrder: response.proposedOrder || null,
        nutritionSummary: response.nutritionSummary || null,
        suggestedActions: response.suggestedActions || []
      };

      setMessages((prev) => [...prev, assistantMsg]);
    } catch (error) {
      setLastFailedMessage(messageText.trim());
      const errorMsg = {
        id: 'err_' + Date.now(),
        role: 'assistant',
        isError: true,
        text: "Sorry, I had trouble processing that request. Please check your connection and try again."
      };
      setMessages((prev) => [...prev, errorMsg]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleRetry = () => {
    if (lastFailedMessage) {
      handleSendMessage(lastFailedMessage);
    }
  };

  const handleClearChat = () => {
    setMessages([
      {
        id: 'welcome_' + Date.now(),
        role: 'assistant',
        text: "Conversation cleared. How can I help you choose your meal today?",
        suggestedActions: QUICK_PROMPTS.slice(0, 3)
      }
    ]);
    setConversationId(null);
    setLastFailedMessage(null);
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage();
    }
  };

  return (
    <div className={`ai-widget-container ${isFullPage ? 'full-page' : ''}`}>
      {/* Floating Toggle Button (if not full page and closed) */}
      {!isFullPage && !isOpen && (
        <button
          className="ai-floating-trigger"
          onClick={() => {
            setIsOpen(true);
            setIsMinimized(false);
          }}
          title="Open Foodie AI Assistant"
        >
          <div className="ai-trigger-icon-wrap">
            <i className="bi bi-robot"></i>
            <span className="ai-trigger-pulse"></span>
          </div>
          <span className="ai-trigger-label d-none d-sm-inline">Foodie AI</span>
        </button>
      )}

      {/* Main Chat Drawer / Window */}
      {(isOpen || isFullPage) && (
        <div className={`ai-chat-window ${isMinimized ? 'minimized' : ''} ${isFullPage ? 'full-page-window' : ''}`}>
          {/* Header */}
          <div className="ai-chat-header">
            <div className="d-flex align-items-center gap-2">
              <div className="ai-avatar-badge">
                <i className="bi bi-robot"></i>
              </div>
              <div>
                <h6 className="ai-chat-title mb-0">Foodie AI</h6>
                <small className="ai-chat-subtitle">Your Personal Food Assistant</small>
              </div>
            </div>

            <div className="ai-header-controls">
              <button
                type="button"
                className="ai-ctrl-btn"
                onClick={handleClearChat}
                title="Restart Chat"
              >
                <i className="bi bi-arrow-clockwise"></i>
              </button>

              {!isFullPage && (
                <>
                  <button
                    type="button"
                    className="ai-ctrl-btn"
                    onClick={() => setIsMinimized(!isMinimized)}
                    title={isMinimized ? "Expand" : "Minimize"}
                  >
                    <i className={`bi ${isMinimized ? 'bi-chevron-up' : 'bi-dash'}`}></i>
                  </button>
                  <button
                    type="button"
                    className="ai-ctrl-btn close-btn"
                    onClick={() => setIsOpen(false)}
                    title="Close"
                  >
                    <i className="bi bi-x-lg"></i>
                  </button>
                </>
              )}
            </div>
          </div>

          {/* Body */}
          {!isMinimized && (
            <>
              <div className="ai-chat-body">
                {/* Messages */}
                {messages.map((msg) => (
                  <div key={msg.id} className={`ai-msg-group ${msg.role === 'user' ? 'user' : 'assistant'}`}>
                    <div className={`ai-bubble ${msg.role} ${msg.isError ? 'error-bubble' : ''}`}>
                      <div className="ai-bubble-text">{msg.text}</div>

                      {/* Error retry */}
                      {msg.isError && lastFailedMessage && (
                        <div className="mt-2 text-end">
                          <button className="btn btn-sm btn-outline-danger" onClick={handleRetry}>
                            <i className="bi bi-arrow-repeat me-1"></i> Retry
                          </button>
                        </div>
                      )}

                      {/* Food Recommendations Cards */}
                      {msg.foodRecommendations && msg.foodRecommendations.length > 0 && (
                        <div className="ai-recommendations-slider mt-2">
                          <div className="ai-cards-track">
                            {msg.foodRecommendations.map((food) => (
                              <FoodRecommendationCard
                                key={food.id}
                                food={food}
                                onAddToProposed={(item) => handleSendMessage(`Add ${item.name} to my proposed order`)}
                              />
                            ))}
                          </div>
                        </div>
                      )}

                      {/* Proposed Order Card */}
                      {msg.proposedOrder && msg.proposedOrder.items && msg.proposedOrder.items.length > 0 && (
                        <div className="mt-2">
                          <ProposedOrderCard
                            proposedOrder={msg.proposedOrder}
                            onOrderUpdated={(updated) => setActiveProposedOrder(updated)}
                          />
                        </div>
                      )}

                      {/* Suggested quick replies */}
                      {msg.suggestedActions && msg.suggestedActions.length > 0 && (
                        <div className="ai-suggested-actions mt-2">
                          {msg.suggestedActions.map((action, idx) => (
                            <button
                              key={idx}
                              type="button"
                              className="ai-action-chip"
                              onClick={() => handleSendMessage(action)}
                            >
                              {action}
                            </button>
                          ))}
                        </div>
                      )}
                    </div>
                  </div>
                ))}

                {/* Loading typing indicator */}
                {isLoading && (
                  <div className="ai-msg-group assistant">
                    <div className="ai-bubble assistant typing-bubble">
                      <div className="ai-typing-indicator">
                        <span></span>
                        <span></span>
                        <span></span>
                      </div>
                      <small className="text-muted ms-2">Finding fresh dishes...</small>
                    </div>
                  </div>
                )}

                <div ref={messagesEndRef} />
              </div>

              {/* Quick Prompts Bar (on start or when idle) */}
              <div className="ai-quick-prompts-bar">
                <span className="ai-quick-label">Try:</span>
                <div className="ai-quick-scroll">
                  {QUICK_PROMPTS.map((prompt, i) => (
                    <button
                      key={i}
                      type="button"
                      className="ai-quick-btn"
                      onClick={() => handleSendMessage(prompt)}
                      disabled={isLoading}
                    >
                      {prompt}
                    </button>
                  ))}
                </div>
              </div>

              {/* Input Footer */}
              <div className="ai-chat-footer">
                <div className="ai-input-wrapper">
                  <textarea
                    ref={inputRef}
                    className="ai-chat-input"
                    rows="1"
                    placeholder="Ask Foodie AI (e.g. 'Spicy dinner for 2 under ₹500')..."
                    value={input}
                    onChange={(e) => setInput(e.target.value)}
                    onKeyDown={handleKeyDown}
                    disabled={isLoading}
                  />
                  <button
                    type="button"
                    className="ai-send-btn"
                    onClick={() => handleSendMessage()}
                    disabled={!input.trim() || isLoading}
                    title="Send"
                  >
                    {isLoading ? (
                      <span className="spinner-border spinner-border-sm"></span>
                    ) : (
                      <i className="bi bi-send-fill"></i>
                    )}
                  </button>
                </div>
                <div className="ai-footer-disclaimer">
                  Verified server pricing • Taxes and delivery calculated at checkout
                </div>
              </div>
            </>
          )}
        </div>
      )}
    </div>
  );
};

export default AiChatWidget;
