from __future__ import annotations

from fastapi import APIRouter, Depends, Path, status
from sqlalchemy.orm import Session

from app import crud
from app.database import get_db
from app.schemas import ContactCreate, ContactRead


router = APIRouter(prefix="/contacts", tags=["contacts"])


@router.get("", response_model=list[ContactRead])
def read_contacts(db: Session = Depends(get_db)):
    return crud.get_contacts(db)


@router.post("", response_model=ContactRead, status_code=status.HTTP_201_CREATED)
def create_contact(contact_in: ContactCreate, db: Session = Depends(get_db)):
    return crud.create_contact(db, contact_in)


@router.get("/{contact_id}", response_model=ContactRead)
def read_contact(
    contact_id: int = Path(..., gt=0),
    db: Session = Depends(get_db),
):
    return crud.get_contact(db, contact_id)


@router.put("/{contact_id}", response_model=ContactRead)
def update_contact(
    contact_in: ContactCreate,
    contact_id: int = Path(..., gt=0),
    db: Session = Depends(get_db),
):
    contact = crud.get_contact(db, contact_id)
    return crud.update_contact(db, contact, contact_in)


@router.delete("/{contact_id}")
def delete_contact(
    contact_id: int = Path(..., gt=0),
    db: Session = Depends(get_db),
):
    contact = crud.get_contact(db, contact_id)
    crud.delete_contact(db, contact)
    return {"message": "Contact deleted successfully."}
