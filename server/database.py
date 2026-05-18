import os
from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession
from sqlalchemy.orm import sessionmaker, declarative_base
from sqlalchemy import Column, Integer, Float, DateTime
from datetime import datetime, timezone

SQLALCHEMY_DATABASE_URL = os.getenv(
    "DATABASE_URL", "sqlite+aiosqlite:///./forestsnap.db"
)

engine = create_async_engine(
    SQLALCHEMY_DATABASE_URL,
    connect_args=(
        {"check_same_thread": False} if "sqlite" in SQLALCHEMY_DATABASE_URL else {}
    ),
)

AsyncSessionLocal = sessionmaker(engine, class_=AsyncSession, expire_on_commit=False)

Base = declarative_base()


class AnalysisRecord(Base):
    __tablename__ = "analysis_records"

    id = Column(Integer, primary_key=True, index=True)
    timestamp = Column(DateTime, default=lambda: datetime.now(timezone.utc))
    latitude = Column(Float, index=True)
    longitude = Column(Float, index=True)
    fuel_load_score = Column(Float)
    dryness_risk_tier = Column(Integer)
    temperature_c = Column(Float)
    humidity_percent = Column(Integer)
    wind_speed_ms = Column(Float)
    wind_direction_deg = Column(Integer)
    final_fire_risk_percent = Column(Float)


async def init_db():
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
