import { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useLanguage } from '../../context/LanguageContext';
import chatApi from '../../api/chatApi';
import { ArrowLeft, Send, User } from 'lucide-react';

export default function ChatConversation() {
  const { receiverId, receiverName } = useParams();
  const { user } = useAuth();
  const { t } = useLanguage();
  const navigate = useNavigate();
  const [messages, setMessages] = useState([]);
  const [text, setText] = useState('');
  const [loading, setLoading] = useState(true);
  const chatBodyRef = useRef(null);

  // Poll for messages every 3 seconds
  useEffect(() => {
    let active = true;

    const fetchHistory = () => {
      chatApi.getChatHistory(parseInt(receiverId, 10))
        .then((data) => {
          if (active) {
            setMessages(data.messages || []);
            setLoading(false);
          }
        })
        .catch((err) => {
          console.error(err);
        });
    };

    fetchHistory();
    const interval = setInterval(fetchHistory, 3000);

    return () => {
      active = false;
      clearInterval(interval);
    };
  }, [receiverId]);

  // Scroll to bottom on messages load
  useEffect(() => {
    if (chatBodyRef.current) {
      chatBodyRef.current.scrollTop = chatBodyRef.current.scrollHeight;
    }
  }, [messages]);

  const handleSend = async (e) => {
    e.preventDefault();
    if (!text.trim()) return;

    const msgText = text;
    setText('');

    try {
      const data = await chatApi.sendMessage(parseInt(receiverId, 10), msgText);
      setMessages((prev) => [...prev, data.data]);
    } catch (err) {
      console.error(err);
    }
  };

  const formatTime = (isoString) => {
    if (!isoString) return '';
    try {
      const date = new Date(isoString);
      return date.toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' });
    } catch (e) {
      return '';
    }
  };

  return (
    <div style={{ maxWidth: '800px', margin: '0 auto' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '16px', textAlign: 'left' }}>
        <button type="button" className="btn btn-outline" style={{ padding: '8px' }} onClick={() => navigate('/dashboard')}>
          <ArrowLeft size={20} />
        </button>
        <h1 style={{ fontSize: '20px', margin: 0, fontWeight: 700 }}>Chat with {decodeURIComponent(receiverName)}</h1>
      </div>

      <div className="chat-window">
        <div className="chat-header">
          <div style={{
            width: '40px',
            height: '40px',
            borderRadius: '50%',
            backgroundColor: 'var(--primary-light)',
            color: 'var(--primary)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center'
          }}>
            <User size={20} />
          </div>
          <div style={{ textAlign: 'left' }}>
            <div style={{ fontWeight: 600, color: 'var(--text-title)' }}>{decodeURIComponent(receiverName)}</div>
            <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Online / Offline</div>
          </div>
        </div>

        <div className="chat-body" ref={chatBodyRef}>
          {loading ? (
            <div style={{ margin: 'auto', color: 'var(--text-muted)' }}>Loading chat history...</div>
          ) : messages.length === 0 ? (
            <div style={{ margin: 'auto', color: 'var(--text-muted)', fontSize: '14px' }}>
              No messages yet. Send a greeting to start the conversation!
            </div>
          ) : (
            messages.map((msg) => {
              const isSent = msg.sender_id === user?.id;
              return (
                <div
                  key={msg.id}
                  className={`chat-bubble ${isSent ? 'sent' : 'received'}`}
                >
                  <div>{msg.content}</div>
                  <span className="chat-time">{formatTime(msg.timestamp)}</span>
                </div>
              );
            })
          )}
        </div>

        <form onSubmit={handleSend} className="chat-footer">
          <input
            type="text"
            className="chat-input"
            value={text}
            onChange={(e) => setText(e.target.value)}
            placeholder="Type your message..."
            required
          />
          <button type="submit" className="btn btn-primary" style={{ padding: '12px 18px' }}>
            <Send size={18} />
          </button>
        </form>
      </div>
    </div>
  );
}
