from __future__ import annotations

from sqlalchemy import select
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from fastapi import HTTPException, status

from app.models import Contact
from app.schemas import ContactCreate


def _duplicate_detail(error: IntegrityError) -> str:
    constraint_name = getattr(getattr(error.orig, "diag", None), "constraint_name", "") or ""
    if "phone" in constraint_name:
        return "Duplicate phone number."
    if "email" in constraint_name:
        return "Duplicate email."
    return "Duplicate contact information."


def get_contacts(db: Session) -> list[Contact]:
    statement = select(Contact).order_by(Contact.created_at.desc(), Contact.id.desc())
    return list(db.scalars(statement).all())


def get_contact(db: Session, contact_id: int) -> Contact:
    contact = db.get(Contact, contact_id)
    if contact is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Contact not found.")
    return contact


def _ensure_unique_fields(db: Session, contact_in: ContactCreate, contact_id: int | None = None) -> None:
    phone_query = select(Contact.id).where(Contact.phone_number == contact_in.phone_number)
    if contact_id is not None:
        phone_query = phone_query.where(Contact.id != contact_id)
    if db.scalar(phone_query) is not None:
        raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail="Duplicate phone number.")

    if contact_in.email is not None:
        email_query = select(Contact.id).where(Contact.email == contact_in.email)
        if contact_id is not None:
            email_query = email_query.where(Contact.id != contact_id)
        if db.scalar(email_query) is not None:
            raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail="Duplicate email.")


def create_contact(db: Session, contact_in: ContactCreate) -> Contact:
    _ensure_unique_fields(db, contact_in)
    contact = Contact(**contact_in.model_dump())
    db.add(contact)
    try:
        db.commit()
    except IntegrityError as error:
        db.rollback()
        raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail=_duplicate_detail(error)) from error
    db.refresh(contact)
    return contact


def update_contact(db: Session, contact: Contact, contact_in: ContactCreate) -> Contact:
    _ensure_unique_fields(db, contact_in, contact.id)
    contact.name = contact_in.name
    contact.phone_number = contact_in.phone_number
    contact.email = contact_in.email
    contact.address = contact_in.address
    try:
        db.commit()
    except IntegrityError as error:
        db.rollback()
        raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail=_duplicate_detail(error)) from error
    db.refresh(contact)
    return contact


def delete_contact(db: Session, contact: Contact) -> None:
    db.delete(contact)
    db.commit()
