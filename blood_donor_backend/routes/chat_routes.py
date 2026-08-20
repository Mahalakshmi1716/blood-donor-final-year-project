from flask import Blueprint, request, jsonify
from extensions import db
from models.message import Message
from models.user import User
from utils.auth import token_required
from sqlalchemy import or_, desc

chat_bp = Blueprint('chat', __name__)

@chat_bp.route('/send', methods=['POST'])
@token_required
def send_message(current_user):
    data = request.get_json()
    receiver_id = data.get('receiver_id')
    content = data.get('content')
    
    if not receiver_id or not content:
        return jsonify({'message': 'Missing receiver_id or content'}), 400
        
    receiver = User.query.get(receiver_id)
    if not receiver:
        return jsonify({'message': 'Receiver not found'}), 404
        
    new_message = Message(
        sender_id=current_user.id,
        receiver_id=receiver_id,
        content=content
    )
    
    db.session.add(new_message)
    db.session.commit()
    
    return jsonify({'message': 'Message sent successfully', 'data': new_message.to_dict()}), 201

@chat_bp.route('/history/<int:other_user_id>', methods=['GET'])
@token_required
def get_chat_history(current_user, other_user_id):
    messages = Message.query.filter(
        or_(
            (Message.sender_id == current_user.id) & (Message.receiver_id == other_user_id),
            (Message.sender_id == other_user_id) & (Message.receiver_id == current_user.id)
        )
    ).order_by(Message.timestamp.asc()).all()
    
    return jsonify({'messages': [m.to_dict() for m in messages]}), 200

@chat_bp.route('/conversations', methods=['GET'])
@token_required
def get_conversations(current_user):
    # Find all unique users this user has chatted with
    sent_messages = Message.query.filter_by(sender_id=current_user.id).all()
    received_messages = Message.query.filter_by(receiver_id=current_user.id).all()
    
    user_ids = set([m.receiver_id for m in sent_messages] + [m.sender_id for m in received_messages])
    
    conversations = []
    for uid in user_ids:
        user = User.query.get(uid)
        if user:
            # get last message
            last_msg = Message.query.filter(
                or_(
                    (Message.sender_id == current_user.id) & (Message.receiver_id == uid),
                    (Message.sender_id == uid) & (Message.receiver_id == current_user.id)
                )
            ).order_by(Message.timestamp.desc()).first()
            
            conversations.append({
                'user_id': user.id,
                'name': user.name,
                'phone_number': user.phone_number,
                'last_message': last_msg.content if last_msg else '',
                'last_message_time': last_msg.timestamp.isoformat() if last_msg else None
            })
            
    # Sort conversations by last message time
    conversations.sort(key=lambda x: x['last_message_time'] or '', reverse=True)
    
    return jsonify({'conversations': conversations}), 200
