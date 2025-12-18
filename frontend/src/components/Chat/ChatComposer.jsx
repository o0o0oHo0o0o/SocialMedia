import React from 'react';

const ChatComposer = ({
  content,
  onContentChange,
  onTextareaKeyDown,
  files,
  isRecording,
  recordingTime,
  startRecording,
  stopRecording,
  cancelRecording,
  onPickFiles,
  removeFile,
  sendMessage
}) => {
  return (
    <footer className="chat-composer">
      {/* File Preview */}
      {files?.length > 0 && (
        <div style={{ padding: '12px', borderBottom: '1px solid #333', display: 'flex', gap: '12px', flexWrap: 'wrap', alignItems: 'flex-start' }}>
          {files.map((f, idx) => {
            const isImage = f.type.startsWith('image');
            const isAudio = f.type.startsWith('audio');
            const objUrl = URL.createObjectURL(f);
            return (
              <div key={idx} style={{ position: 'relative', display: 'flex', flexDirection: 'column', alignItems: 'flex-start' }}>
                <div style={{ borderRadius: '8px', overflow: 'hidden', border: '2px solid #667eea', background: '#1a1a2e', display: 'flex', alignItems: 'center', gap: '8px', padding: '8px 12px', fontSize: '12px' }}>
                  {isImage ? (
                    <>
                      <img src={objUrl} alt={f.name} style={{ width: '50px', height: '50px', borderRadius: '4px', objectFit: 'cover', cursor: 'pointer' }} onClick={() => window.open(objUrl, '_blank')} />
                      <div style={{ flex: 1, maxWidth: '150px' }}>
                        <div style={{ wordBreak: 'break-word', color: '#fff', fontSize: '11px' }}>{f.name}</div>
                        <div style={{ opacity: 0.7, fontSize: '10px', marginTop: '2px' }}>{(f.size / 1024).toFixed(2)} KB</div>
                      </div>
                    </>
                  ) : isAudio ? (
                    <>
                      <span style={{ fontSize: '24px' }}>🎙️</span>
                      <div style={{ flex: 1, maxWidth: '150px' }}>
                        <div style={{ wordBreak: 'break-word', color: '#fff', fontSize: '11px' }}>{f.name}</div>
                        <div style={{ opacity: 0.7, fontSize: '10px', marginTop: '2px' }}>{(f.size / 1024).toFixed(2)} KB</div>
                      </div>
                    </>
                  ) : (
                    <>
                      <span style={{ fontSize: '24px' }}>📎</span>
                      <div style={{ flex: 1, maxWidth: '150px' }}>
                        <div style={{ wordBreak: 'break-word', color: '#fff', fontSize: '11px' }}>{f.name}</div>
                        <div style={{ opacity: 0.7, fontSize: '10px', marginTop: '2px' }}>{(f.size / 1024).toFixed(2)} KB</div>
                      </div>
                    </>
                  )}
                </div>
                <button onClick={() => removeFile(idx)} style={{ marginTop: '6px', background: '#ff4444', color: 'white', border: 'none', borderRadius: '4px', padding: '4px 8px', cursor: 'pointer', fontSize: '12px', fontWeight: 'bold' }}>✕ Remove</button>
              </div>
            );
          })}
        </div>
      )}

      {/* Recording UI */}
      {isRecording ? (
        <div style={{ padding: '12px', borderBottom: '1px solid #333', display: 'flex', alignItems: 'center', gap: '12px', background: 'rgba(220, 20, 60, 0.1)', borderRadius: '8px', margin: '0 12px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flex: 1 }}>
            <span style={{ fontSize: '20px', animation: 'pulse 1s infinite' }}>🔴</span>
            <span style={{ color: '#ff4444', fontWeight: 'bold', fontSize: '14px' }}>Recording {recordingTime}</span>
          </div>
          <button onClick={stopRecording} style={{ padding: '8px 12px', background: '#ff4444', color: '#fff', border: 'none', borderRadius: '6px', cursor: 'pointer' }}>Stop</button>
          <button onClick={cancelRecording} style={{ padding: '8px 12px', background: '#444', color: '#fff', border: 'none', borderRadius: '6px', cursor: 'pointer', marginLeft: '8px' }}>Cancel</button>
        </div>
      ) : null}

      <div style={{ display: 'flex', gap: '8px', padding: '12px' }}>
        <textarea
          value={content}
          onChange={onContentChange}
          onKeyDown={onTextareaKeyDown}
          placeholder="Nhập tin nhắn..."
          style={{ flex: 1, padding: '8px', borderRadius: '8px', background: 'rgba(255,255,255,0.03)', color: '#fff', border: '1px solid rgba(255,255,255,0.06)', minHeight: '48px' }}
        />
        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
          <input id="chat-files" type="file" multiple onChange={onPickFiles} style={{ display: 'none' }} />
          <label htmlFor="chat-files" style={{ padding: '8px 12px', background: '#667eea', color: '#fff', borderRadius: '6px', cursor: 'pointer' }}>📎</label>
          <button onClick={startRecording} style={{ padding: '8px 12px', background: '#333', color: '#fff', borderRadius: '6px', cursor: 'pointer' }}>🎤</button>
          <button onClick={sendMessage} style={{ padding: '8px 12px', background: '#00b894', color: '#fff', borderRadius: '6px', cursor: 'pointer' }}>Gửi</button>
        </div>
      </div>
    </footer>
  );
};

export default ChatComposer;
